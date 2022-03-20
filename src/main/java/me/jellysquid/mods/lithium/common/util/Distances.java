package me.jellysquid.mods.lithium.common.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkSection;

public class Distances {

    public static double getMinChunkToBlockDistanceL2Sq(BlockPos origin, int chunkX, int chunkZ) {
        int chunkMinX = ChunkSection.blockCoordFromChunkCoord(chunkX);
        int chunkMinZ = ChunkSection.blockCoordFromChunkCoord(chunkZ);

        int xDistance = origin.getX() - chunkMinX;
        if (xDistance > 0) {
            xDistance = Math.max(0, xDistance - 15);
        }
        int zDistance = origin.getZ() - chunkMinZ;
        if (zDistance > 0) {
            zDistance = Math.max(0, zDistance - 15);
        }

        return xDistance * xDistance + zDistance * zDistance;
    }

    public static boolean isWithinSquareRadius(BlockPos origin, int radius, BlockPos pos) {
        return Math.abs(pos.getX() - origin.getX()) <= radius &&
                Math.abs(pos.getZ() - origin.getZ()) <= radius;
    }

    public static boolean isWithinCircleRadius(BlockPos origin, double radiusSq, BlockPos pos) {
        return origin.getSquaredDistance(pos) <= radiusSq;
    }
}
