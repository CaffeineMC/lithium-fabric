package me.jellysquid.mods.lithium.common.ai.pathing;

public class NodePosition {
    private final int x;
    private final int y;
    private final int z;

    public NodePosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
}