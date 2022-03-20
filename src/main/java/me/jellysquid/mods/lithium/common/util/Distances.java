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

    public static double getMinSectionToBlockDistanceL2Sq(BlockPos origin, int chunkX, int chunkY, int chunkZ) {
        int chunkMinX = ChunkSection.blockCoordFromChunkCoord(chunkX);
        int chunkMinY = ChunkSection.blockCoordFromChunkCoord(chunkY);
        int chunkMinZ = ChunkSection.blockCoordFromChunkCoord(chunkZ);

        int xDistance = origin.getX() - chunkMinX;
        if (xDistance > 0) {
            xDistance = Math.max(0, xDistance - 15);
        }
        int yDistance = origin.getY() - chunkMinY;
        if (yDistance > 0) {
            yDistance = Math.max(0, yDistance - 15);
        }
        int zDistance = origin.getZ() - chunkMinZ;
        if (zDistance > 0) {
            zDistance = Math.max(0, zDistance - 15);
        }

        return xDistance * xDistance + yDistance * yDistance + zDistance * zDistance;
    }

    public static double getMaxSectionToBlockDistanceL2Sq(BlockPos origin, int chunkX, int chunkY, int chunkZ) {
        int chunkMinX = ChunkSection.blockCoordFromChunkCoord(chunkX);
        int chunkMinY = ChunkSection.blockCoordFromChunkCoord(chunkY);
        int chunkMinZ = ChunkSection.blockCoordFromChunkCoord(chunkZ);

        int xDistance = origin.getX() - chunkMinX;
        if (xDistance <= 7) {
            xDistance -= 15;
        }
        int yDistance = origin.getY() - chunkMinY;
        if (yDistance <= 7) {
            yDistance -= 15;
        }
        int zDistance = origin.getZ() - chunkMinZ;
        if (zDistance > 0) {
            zDistance -= 15;
        }

        return xDistance * xDistance + yDistance * yDistance + zDistance * zDistance;
    }

    public static boolean isWithinSquareRadius(BlockPos origin, int radius, BlockPos pos) {
        return Math.abs(pos.getX() - origin.getX()) <= radius &&
                Math.abs(pos.getZ() - origin.getZ()) <= radius;
    }

    public static boolean isWithinCircleRadius(BlockPos origin, double radiusSq, BlockPos pos) {
        return origin.getSquaredDistance(pos) <= radiusSq;
    }
}
