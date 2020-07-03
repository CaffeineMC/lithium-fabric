package me.jellysquid.mods.lithium.mixin.world.chunk_ticking;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import me.jellysquid.mods.lithium.common.world.PlayerChunkWatchingManagerIterable;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.PlayerChunkWatchingManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerChunkWatchingManager.class)
public class PlayerChunkWatchingManagerMixin implements PlayerChunkWatchingManagerIterable {
    @Shadow
    @Final
    private Object2BooleanMap<ServerPlayerEntity> watchingPlayers;

    @Override
    public Iterable<ServerPlayerEntity> getPlayers() {
        return this.watchingPlayers.keySet();
    }
}
