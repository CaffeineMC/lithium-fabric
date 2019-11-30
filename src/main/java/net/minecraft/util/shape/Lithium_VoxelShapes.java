package net.minecraft.util.shape;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import me.jellysquid.mods.lithium.common.cache.EntityChunkCache;
import me.jellysquid.mods.lithium.common.shape.IndirectListPair;
import me.jellysquid.mods.lithium.common.shape.IndirectListPairCache;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityContext;
import net.minecraft.util.BooleanBiFunction;
import net.minecraft.util.math.*;

import java.util.stream.Stream;

public class Lithium_VoxelShapes {
    public static double calculateSoftOffset(Direction.Axis axis, Box box, EntityChunkCache world, double double_1, EntityContext context, Stream<VoxelShape> stream) {
        return method_17944(box, world, double_1, context, AxisCycleDirection.between(axis, Direction.Axis.Z), stream);
    }

    public static double method_17944(Box box, EntityChunkCache chunks, double out, EntityContext context, AxisCycleDirection cycleDirection, Stream<VoxelShape> stream) {
        if (!(box.getXSize() >= 1.0E-6D) || !(box.getYSize() >= 1.0E-6D) || !(box.getZSize() >= 1.0E-6D)) {
            return out;
        }

        if (Math.abs(out) < 1.0E-7D) {
            return 0.0D;
        }

        AxisCycleDirection oppositeCycleDirection = cycleDirection.opposite();

        Direction.Axis xAxis = oppositeCycleDirection.cycle(Direction.Axis.X);
        Direction.Axis yAxis = oppositeCycleDirection.cycle(Direction.Axis.Y);
        Direction.Axis zAxis = oppositeCycleDirection.cycle(Direction.Axis.Z);

        BlockPos.Mutable pos = new BlockPos.Mutable();

        int minOnX = MathHelper.floor(box.getMin(xAxis) - 1.0E-7D) - 1;
        int maxOnX = MathHelper.floor(box.getMax(xAxis) + 1.0E-7D) + 1;
        int minOnY = MathHelper.floor(box.getMin(yAxis) - 1.0E-7D) - 1;
        int maxOnY = MathHelper.floor(box.getMax(yAxis) + 1.0E-7D) + 1;

        double zMin = box.getMin(zAxis) - 1.0E-7D;
        double zMax = box.getMax(zAxis) + 1.0E-7D;

        boolean isPositive = out > 0.0D;

        int minOnZ = isPositive ? MathHelper.floor(box.getMax(zAxis) - 1.0E-7D) - 1 : MathHelper.floor(box.getMin(zAxis) + 1.0E-7D) + 1;
        int maxOnZ = method_17943(out, zMin, zMax);

        int inc = isPositive ? 1 : -1;
        int z = minOnZ;

        while (true) {
            if (isPositive) {
                if (z > maxOnZ) {
                    break;
                }
            } else if (z < maxOnZ) {
                break;
            }

            for (int x = minOnX; x <= maxOnX; ++x) {
                for (int y = minOnY; y <= maxOnY; ++y) {
                    int hitAxis = 0;

                    if (x == minOnX || x == maxOnX) {
                        ++hitAxis;
                    }

                    if (y == minOnY || y == maxOnY) {
                        ++hitAxis;
                    }

                    if (z == minOnZ || z == maxOnZ) {
                        ++hitAxis;
                    }

                    if (hitAxis < 3) {
                        pos.set(oppositeCycleDirection, x, y, z);

                        BlockState blockState_1 = chunks.getBlockState(pos);

                        if ((hitAxis != 1 || blockState_1.method_17900()) && (hitAxis != 2 || blockState_1.getBlock() == Blocks.MOVING_PISTON)) {
                            out = blockState_1.getCollisionShape(chunks.getWorld(), pos, context)
                                    .method_1108(zAxis, box.offset(-pos.getX(), -pos.getY(), -pos.getZ()), out);

                            if (Math.abs(out) < 1.0E-7D) {
                                return 0.0D;
                            }

                            maxOnZ = method_17943(out, zMin, zMax);
                        }
                    }
                }
            }

            z += inc;
        }

        // This is a silly hack to allow the lambda to mutate the return value.
        double[] ugly = new double[]{out};

        stream.forEach((shape) -> {
            ugly[0] = shape.method_1108(zAxis, box, ugly[0]);
        });

        return ugly[0];

    }

    private static int method_17943(double double_1, double double_2, double double_3) {
        return double_1 > 0.0D ? MathHelper.floor(double_3 + double_1) + 1 : MathHelper.floor(double_2 + double_1) - 1;
    }

    public static boolean matchesAnywhere(VoxelShape aShape, VoxelShape bShape, BooleanBiFunction func) {
        if (func.apply(false, false)) {
            throw new IllegalArgumentException();
        }

        if (aShape == bShape) {
            return func.apply(true, true);
        }

        if (aShape.isEmpty()) {
            return func.apply(false, !bShape.isEmpty());
        }

        if (bShape.isEmpty()) {
            return func.apply(!aShape.isEmpty(), false);
        }

        boolean ab = func.apply(true, false);
        boolean ba = func.apply(false, true);

        for (Direction.Axis axis : AxisCycleDirection.AXES) {
            if (aShape.getMaximum(axis) < bShape.getMinimum(axis) - 1.0E-7D) {
                return ab || ba;
            }

            if (bShape.getMaximum(axis) < aShape.getMinimum(axis) - 1.0E-7D) {
                return ab || ba;
            }
        }

        DoubleListPair listX = createListPair(1, aShape.getPointPositions(Direction.Axis.X), bShape.getPointPositions(Direction.Axis.X), ab, ba);
        DoubleListPair listY = createListPair(listX.getMergedList().size() - 1, aShape.getPointPositions(Direction.Axis.Y), bShape.getPointPositions(Direction.Axis.Y), ab, ba);
        DoubleListPair listZ = createListPair((listX.getMergedList().size() - 1) * (listY.getMergedList().size() - 1), aShape.getPointPositions(Direction.Axis.Z), bShape.getPointPositions(Direction.Axis.Z), ab, ba);

        boolean ret = matchesAnywhere(listX, listY, listZ, aShape.voxels, bShape.voxels, func);

        if (listX instanceof IndirectListPair) IndirectListPairCache.release((IndirectListPair) listX);
        if (listY instanceof IndirectListPair) IndirectListPairCache.release((IndirectListPair) listY);
        if (listZ instanceof IndirectListPair) IndirectListPairCache.release((IndirectListPair) listZ);

        return ret;
    }

    // TODO: This allocates a lot of inner classes each iteration. How do we avoid that?
    private static boolean matchesAnywhere(DoubleListPair x, DoubleListPair y, DoubleListPair z, VoxelSet a, VoxelSet b, BooleanBiFunction func) {
        return !x.forAllOverlappingSections((i11, i12, i13) -> {
            return y.forAllOverlappingSections((i21, i22, i23) -> {
                return z.forAllOverlappingSections((i31, i32, i33) -> {
                    return !func.apply(a.inBoundsAndContains(i11, i21, i31), b.inBoundsAndContains(i12, i22, i32));
                });
            });
        });
    }

    private static DoubleListPair createListPair(int int_1, DoubleList a, DoubleList b, boolean flag1, boolean flag2) {
        int aLastIdx = a.size() - 1;
        int bLastIdx = b.size() - 1;

        if (a instanceof FractionalDoubleList && b instanceof FractionalDoubleList) {
            long lcm = VoxelShapes.lcm(aLastIdx, bLastIdx);

            if ((long) int_1 * lcm <= 256L) {
                return new FractionalDoubleListPair(aLastIdx, bLastIdx);
            }
        }

        if (a.getDouble(aLastIdx) < b.getDouble(0) - 1.0E-7D) {
            return new DisjointDoubleListPair(a, b, false);
        }

        if (b.getDouble(bLastIdx) < a.getDouble(0) - 1.0E-7D) {
            return new DisjointDoubleListPair(b, a, true);
        }

        if (Lithium_VoxelShapes.equals(a, b)) {
            if (a instanceof IdentityListMerger) {
                return (DoubleListPair) a;
            }

            if (b instanceof IdentityListMerger) {
                return (DoubleListPair) b;
            }

            return new IdentityListMerger(a);
        }

        return IndirectListPairCache.create(a, b, flag1, flag2);
    }

    private static boolean equals(DoubleList a, DoubleList b) {
        if (a.size() != b.size()) {
            return false;
        }

        int size = a.size();

        for (int i = 0; i < size; i++) {
            if (a.getDouble(i) != b.getDouble(i)) {
                return false;
            }
        }

        return true;
    }
}
