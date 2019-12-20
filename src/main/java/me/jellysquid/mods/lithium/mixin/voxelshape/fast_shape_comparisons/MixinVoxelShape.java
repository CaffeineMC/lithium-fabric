package me.jellysquid.mods.lithium.mixin.voxelshape.fast_shape_comparisons;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.util.math.AxisCycleDirection;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelSet;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(VoxelShape.class)
public abstract class MixinVoxelShape {
    @Shadow
    @Final
    protected VoxelSet voxels;

    @Shadow
    public abstract boolean isEmpty();

    @Shadow
    protected abstract double getPointPosition(Direction.Axis axis, int index);


    @Shadow
    protected abstract DoubleList getPointPositions(Direction.Axis axis);

    /**
     * Delays calculating the values for min/maxXYZ until absolutely necessary.
     *
     * @reason Use optimized implementation
     * @author JellySquid
     */
    @Overwrite
    public double calculateMaxDistance(AxisCycleDirection axisCycle, Box box, double maxDist) {
        if (this.isEmpty()) {
            return maxDist;
        }

        if (Math.abs(maxDist) < 1.0E-7D) {
            return 0.0D;
        }

        AxisCycleDirection axisCycleDirection = axisCycle.opposite();

        Direction.Axis axisX = axisCycleDirection.cycle(Direction.Axis.X);
        Direction.Axis axisY = axisCycleDirection.cycle(Direction.Axis.Y);
        Direction.Axis axisZ = axisCycleDirection.cycle(Direction.Axis.Z);

        int minY = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int minZ = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        int x, y, z;

        double dist;

        if (maxDist > 0.0D) {
            double max = box.getMax(axisX);
            int maxIdx = this.getCoordIndex(axisX, max - 1.0E-7D);

            int maxX = this.voxels.getSize(axisX);

            for (x = maxIdx + 1; x < maxX; ++x) {
                minY = minY == Integer.MIN_VALUE ? Math.max(0, this.getCoordIndex(axisY, box.getMin(axisY) + 1.0E-7D)) : minY;
                maxY = maxY == Integer.MIN_VALUE ? Math.min(this.voxels.getSize(axisY), this.getCoordIndex(axisY, box.getMax(axisY) - 1.0E-7D) + 1) : maxY;

                for (y = minY; y < maxY; ++y) {
                    minZ = minZ == Integer.MIN_VALUE ? Math.max(0, this.getCoordIndex(axisZ, box.getMin(axisZ) + 1.0E-7D)) : minZ;
                    maxZ = maxZ == Integer.MIN_VALUE ? Math.min(this.voxels.getSize(axisZ), this.getCoordIndex(axisZ, box.getMax(axisZ) - 1.0E-7D) + 1) : maxZ;

                    for (z = minZ; z < maxZ; ++z) {
                        if (this.voxels.inBoundsAndContains(axisCycleDirection, x, y, z)) {
                            dist = this.getPointPosition(axisX, x) - max;

                            if (dist >= -1.0E-7D) {
                                maxDist = Math.min(maxDist, dist);
                            }

                            return maxDist;
                        }
                    }
                }
            }
        } else if (maxDist < 0.0D) {
            double min = box.getMin(axisX);
            int minIdx = this.getCoordIndex(axisX, min + 1.0E-7D);

            for (x = minIdx - 1; x >= 0; --x) {
                minY = minY == Integer.MIN_VALUE ? Math.max(0, this.getCoordIndex(axisY, box.getMin(axisY) + 1.0E-7D)) : minY;
                maxY = maxY == Integer.MIN_VALUE ? Math.min(this.voxels.getSize(axisY), this.getCoordIndex(axisY, box.getMax(axisY) - 1.0E-7D) + 1) : maxY;

                for (y = minY; y < maxY; ++y) {
                    minZ = minZ == Integer.MIN_VALUE ? Math.max(0, this.getCoordIndex(axisZ, box.getMin(axisZ) + 1.0E-7D)) : minZ;
                    maxZ = maxZ == Integer.MIN_VALUE ? Math.min(this.voxels.getSize(axisZ), this.getCoordIndex(axisZ, box.getMax(axisZ) - 1.0E-7D) + 1) : maxZ;

                    for (z = minZ; z < maxZ; ++z) {
                        if (this.voxels.inBoundsAndContains(axisCycleDirection, x, y, z)) {
                            dist = this.getPointPosition(axisX, x + 1) - min;

                            if (dist <= 1.0E-7D) {
                                maxDist = Math.max(maxDist, dist);
                            }

                            return maxDist;
                        }
                    }
                }
            }
        }

        return maxDist;
    }

    /**
     * In-lines the lambda passed to MathHelper#binarySearch
     *
     * @reason Use faster implementation
     * @author JellySquid
     */
    @Overwrite
    public int getCoordIndex(Direction.Axis axis, double coord) {
        DoubleList list = this.getPointPositions(axis);

        int size = this.voxels.getSize(axis);

        int start = 0;
        int end = size + 1 - start;

        while (end > 0) {
            int middle = end / 2;
            int idx = start + middle;

            if (idx >= 0 && (idx > size || coord < list.getDouble(idx))) {
                end = middle;
            } else {
                start = idx + 1;
                end -= middle + 1;
            }
        }

        return start - 1;
    }

}
