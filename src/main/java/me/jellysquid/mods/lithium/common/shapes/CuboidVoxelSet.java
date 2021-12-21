package me.jellysquid.mods.lithium.common.shapes;

import me.jellysquid.mods.lithium.common.util.math.FastMath;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelSet;

public class CuboidVoxelSet extends VoxelSet {
    private final int minX, minY, minZ, maxX, maxY, maxZ;

    protected CuboidVoxelSet(int xSize, int ySize, int zSize, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        super(xSize, ySize, zSize);

        this.minX = (int) FastMath.round(minX * xSize);
        this.maxX = (int) FastMath.round(maxX * xSize);
        this.minY = (int) FastMath.round(minY * ySize);
        this.maxY = (int) FastMath.round(maxY * ySize);
        this.minZ = (int) FastMath.round(minZ * zSize);
        this.maxZ = (int) FastMath.round(maxZ * zSize);
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return x >= this.minX && x < this.maxX &&
                y >= this.minY && y < this.maxY &&
                z >= this.minZ && z < this.maxZ;
    }

    @Override
    public void set(int x, int y, int z) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMin(Direction.Axis axis) {
        return axis.choose(this.minX, this.minY, this.minZ);
    }

    @Override
    public int getMax(Direction.Axis axis) {
        return axis.choose(this.maxX, this.maxY, this.maxZ);
    }

    @Override
    public boolean isEmpty() {
        return this.minX >= this.maxX || this.minY >= this.maxY || this.minZ >= this.maxZ;
    }

}
