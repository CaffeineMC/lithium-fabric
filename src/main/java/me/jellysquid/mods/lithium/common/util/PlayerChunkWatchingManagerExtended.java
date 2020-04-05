package me.jellysquid.mods.lithium.common.util;


import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerChunkWatchingManagerExtended {
    Iterable<ServerPlayerEntity> getPlayers();
}
