package me.jellysquid.mods.lithium.mixin.voxelshape.fast_shape_comparisons;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.util.math.AxisCycleDirection;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelSet;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(VoxelShape.class)
public abstract class MixinVoxelShape {
    private static final Direction.Axis[] ALL_AXIS = Direction.Axis.values();

    private static final double POSITIVE_EPSILON = +1.0E-7D;
    private static final double NEGATIVE_EPSILON = -1.0E-7D;

    @Shadow
    @Final
    protected VoxelSet voxels;

    @Shadow
    public abstract boolean isEmpty();

    @Shadow
    protected abstract double getPointPosition(Direction.Axis axis, int index);

    @Shadow
    protected abstract DoubleList getPointPositions(Direction.Axis axis);

    private int simpleFlag;

    /**
     * @reason Use optimized implementation
     * @author JellySquid
     */
    @Overwrite
    public double calculateMaxDistance(AxisCycleDirection cycleDirection, Box box, double maxDist) {
        if (this.isEmpty()) {
            return maxDist;
        }

        if (Math.abs(maxDist) < POSITIVE_EPSILON) {
            return 0.0D;
        }

        if (this.isSimpleShape()) {
            return this.calculateMaxDistanceInnerSimple(cycleDirection.opposite(), box, maxDist);
        } else {
            return this.calculateMaxDistanceInner(cycleDirection.opposite(), box, maxDist);
        }
    }

    protected boolean isSimpleShape() {
        // Check to see if this is a simple shape with only one box. If we were able to extend VoxelShape, we could
        // make this an implementation detail of a special shape. This property is cached and lazily initialized.
        if (this.simpleFlag == 0) {
            this.simpleFlag = this.calculateSimpleShape() ? 1 : -1;
        }

        return this.simpleFlag == 1;
    }

    protected boolean calculateSimpleShape() {
        // The full cube shape is always simple.
        if ((Object) this == VoxelShapes.fullCube()) {
            return true;
        }

        // Check that the shape only contains two coordinates (a min/max of a simple cube) on each axis. If so, we're
        // a simple shape.
        for (Direction.Axis axis : ALL_AXIS) {
            DoubleList list = this.getPointPositions(axis);

            if (list.size() != 2) {
                return false;
            }
        }

        return true;
    }

    /**
     * [VanillaCopy] VoxelShape#calculateMaxDistance
     * Avoids performing a search for a coordinate's index until absolutely necessary.
     */
    private double calculateMaxDistanceInner(AxisCycleDirection cycle, Box box, double maxDist) {
        Direction.Axis axisX = cycle.cycle(Direction.Axis.X);
        Direction.Axis axisY = cycle.cycle(Direction.Axis.Y);
        Direction.Axis axisZ = cycle.cycle(Direction.Axis.Z);

        int minY = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int minZ = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        int x, y, z;

        double dist;

        if (maxDist > 0.0D) {
            double max = box.getMax(axisX);
            int maxIdx = this.getCoordIndex(axisX, max - POSITIVE_EPSILON);

            int maxX = this.voxels.getSize(axisX);

            for (x = maxIdx + 1; x < maxX; ++x) {
                minY = minY == Integer.MIN_VALUE ? Math.max(0, this.getCoordIndex(axisY, box.getMin(axisY) + POSITIVE_EPSILON)) : minY;
                maxY = maxY == Integer.MIN_VALUE ? Math.min(this.voxels.getSize(axisY), this.getCoordIndex(axisY, box.getMax(axisY) - POSITIVE_EPSILON) + 1) : maxY;

                for (y = minY; y < maxY; ++y) {
                    minZ = minZ == Integer.MIN_VALUE ? Math.max(0, this.getCoordIndex(axisZ, box.getMin(axisZ) + POSITIVE_EPSILON)) : minZ;
                    maxZ = maxZ == Integer.MIN_VALUE ? Math.min(this.voxels.getSize(axisZ), this.getCoordIndex(axisZ, box.getMax(axisZ) - POSITIVE_EPSILON) + 1) : maxZ;

                    for (z = minZ; z < maxZ; ++z) {
                        if (this.voxels.inBoundsAndContains(cycle, x, y, z)) {
                            dist = this.getPointPosition(axisX, x) - max;

                            if (dist >= NEGATIVE_EPSILON) {
                                maxDist = Math.min(maxDist, dist);
                            }

                            return maxDist;
                        }
                    }
                }
            }
        } else if (maxDist < 0.0D) {
            double min = box.getMin(axisX);
            int minIdx = this.getCoordIndex(axisX, min + POSITIVE_EPSILON);

            for (x = minIdx - 1; x >= 0; --x) {
                minY = minY == Integer.MIN_VALUE ? Math.max(0, this.getCoordIndex(axisY, box.getMin(axisY) + POSITIVE_EPSILON)) : minY;
                maxY = maxY == Integer.MIN_VALUE ? Math.min(this.voxels.getSize(axisY), this.getCoordIndex(axisY, box.getMax(axisY) - POSITIVE_EPSILON) + 1) : maxY;

                for (y = minY; y < maxY; ++y) {
                    minZ = minZ == Integer.MIN_VALUE ? Math.max(0, this.getCoordIndex(axisZ, box.getMin(axisZ) + POSITIVE_EPSILON)) : minZ;
                    maxZ = maxZ == Integer.MIN_VALUE ? Math.min(this.voxels.getSize(axisZ), this.getCoordIndex(axisZ, box.getMax(axisZ) - POSITIVE_EPSILON) + 1) : maxZ;

                    for (z = minZ; z < maxZ; ++z) {
                        if (this.voxels.inBoundsAndContains(cycle, x, y, z)) {
                            dist = this.getPointPosition(axisX, x + 1) - min;

                            if (dist <= POSITIVE_EPSILON) {
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
     * Takes advantage of the fact that the shape is only a single cube. This allows us to replace the binary search
     * with a simple check to select either the min/max coordinate.
     */
    private double calculateMaxDistanceInnerSimple(AxisCycleDirection cycle, Box box, double maxDist) {
        Direction.Axis xAxis = cycle.cycle(Direction.Axis.X);
        DoubleList xList = this.getPointPositions(xAxis);

        double penetration;

        if (maxDist > 0.0D) {
            penetration = xList.getDouble(0) - box.getMax(xAxis);

            if (penetration < NEGATIVE_EPSILON || maxDist < penetration) {
                return maxDist;
            }
        } else {
            penetration = xList.getDouble(1) - box.getMin(xAxis);

            if (penetration > POSITIVE_EPSILON || maxDist > penetration) {
                return maxDist;
            }
        }

        Direction.Axis yAxis = cycle.cycle(Direction.Axis.Y);
        Direction.Axis zAxis = cycle.cycle(Direction.Axis.Z);

        DoubleList yList = this.getPointPositions(yAxis);
        DoubleList zList = this.getPointPositions(zAxis);

        if (yList.getDouble(0) + POSITIVE_EPSILON < box.getMax(yAxis) && box.getMin(yAxis) + POSITIVE_EPSILON < yList.getDouble(1)) {
            if (zList.getDouble(0) + POSITIVE_EPSILON < box.getMax(zAxis) && box.getMin(zAxis) + POSITIVE_EPSILON < zList.getDouble(1)) {
                if (penetration < POSITIVE_EPSILON || penetration > NEGATIVE_EPSILON) {
                    return 0.0D;
                }

                return penetration;
            }
        }

        return maxDist;
    }

    /**
     * In-lines the lambda passed to MathHelper#binarySearch. Simplifies the implementation very slightly for additional
     * speed.
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
