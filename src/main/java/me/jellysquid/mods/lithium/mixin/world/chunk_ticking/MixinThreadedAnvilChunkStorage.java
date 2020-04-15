package me.jellysquid.mods.lithium.mixin.world.chunk_ticking;

import me.jellysquid.mods.lithium.common.world.PlayerChunkWatchingManagerExtended;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.PlayerChunkWatchingManager;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class MixinThreadedAnvilChunkStorage {
    @Shadow
    @Final
    private ThreadedAnvilChunkStorage.TicketManager ticketManager;

    @Shadow
    @Final
    private PlayerChunkWatchingManager playerChunkWatchingManager;

    @Shadow
    private static double getSquaredDistance(ChunkPos pos, Entity entity) {
        throw new UnsupportedOperationException();
    }

    /**
     * @reason Use optimized implementation
     * @author JellySquid
     */
    @Overwrite
    @SuppressWarnings("ConstantConditions")
    public boolean isTooFarFromPlayersToSpawnMobs(ChunkPos pos) {
        long key = pos.toLong();

        if (!this.ticketManager.method_20800(key)) {
            return true;
        }

        for (ServerPlayerEntity player : ((PlayerChunkWatchingManagerExtended) (Object) this.playerChunkWatchingManager).getPlayers()) {
            if (!player.isSpectator() && getSquaredDistance(pos, player) < 16384.0D) {
                return false;
            }
        }

        return true;
    }
}
