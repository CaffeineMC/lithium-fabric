package me.jellysquid.mods.lithium.common.world;


import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerChunkWatchingManagerExtended {
    /**
     * See {@link net.minecraft.server.world.PlayerChunkWatchingManager#getPlayersWatchingChunk(long)}. The position
     * variant is actually never used (presumably because it's not yet implemented?)
     *
     * TODO: Use an index to avoid iterating over all players on the server
     */
    Iterable<ServerPlayerEntity> getPlayers();
}
