package me.jellysquid.mods.lithium.common.chunk;

import java.util.HashSet;

import net.minecraft.util.math.ChunkPos;

public class SlimeChunkStorage{

    private static HashSet<ChunkPos> slimeChunks = new HashSet<ChunkPos>();

    public static boolean checkSlimeChunk(ChunkPos chunkChecking) {
        return slimeChunks.contains(chunkChecking);
    }

    public static void addSlimeChunk(ChunkPos chunk) {
        slimeChunks.add(chunk);
    }

    public static void unloadSlimeChunk(ChunkPos chunk) {
        slimeChunks.remove(chunk);
    }

}