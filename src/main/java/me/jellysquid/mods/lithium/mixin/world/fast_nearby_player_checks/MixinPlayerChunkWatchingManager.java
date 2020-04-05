package me.jellysquid.mods.lithium.mixin.world.fast_nearby_player_checks;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import me.jellysquid.mods.lithium.common.util.PlayerChunkWatchingManagerExtended;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.PlayerChunkWatchingManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerChunkWatchingManager.class)
public class MixinPlayerChunkWatchingManager implements PlayerChunkWatchingManagerExtended {
    @Shadow
    @Final
    private Object2BooleanMap<ServerPlayerEntity> watchingPlayers;

    @Override
    public Iterable<ServerPlayerEntity> getPlayers() {
        return this.watchingPlayers.keySet();
    }
}
