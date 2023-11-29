package me.jellysquid.mods.lithium.common;

import me.jellysquid.mods.lithium.common.entity.movement.ChunkAwareBlockCollisionSweeper;

public class LithiumDebugInfo {
    public static volatile ChunkAwareBlockCollisionSweeper blockCollisionDebugInfo;
    public static long lastBlockCollisionDebugInfoUpdate = 0;


    public static String getBlockCollisionDebugInfo() {
        if (blockCollisionDebugInfo != null) {
            return blockCollisionDebugInfo.getDebugInfo();
        }
        return null;
    }

    public static String getElapsedTime() {
        return "Block collision started "  + String.format("%.2f", (System.currentTimeMillis() - lastBlockCollisionDebugInfoUpdate) / 1000.0) + " seconds ago.";
    }

    public static void setBlockCollisionDebugInfo(ChunkAwareBlockCollisionSweeper blockCollisionDebugInfo) {
        LithiumDebugInfo.blockCollisionDebugInfo = blockCollisionDebugInfo;
        LithiumDebugInfo.lastBlockCollisionDebugInfoUpdate = System.currentTimeMillis();
    }
}
