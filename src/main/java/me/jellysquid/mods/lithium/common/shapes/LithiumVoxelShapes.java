package me.jellysquid.mods.lithium.common.shapes;

import me.jellysquid.mods.lithium.common.cache.EntityChunkCache;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityContext;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import java.util.stream.Stream;

public class LithiumVoxelShapes {
    public static double calculateSoftOffset(Direction.Axis axis, Box box, EntityChunkCache world, double initial, EntityContext context, Stream<VoxelShape> stream) {
        return method_17944(box, world, initial, context, AxisCycleDirection.between(axis, Direction.Axis.Z), stream);
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
        int maxOnZ = clamp(out, zMin, zMax);

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

                            maxOnZ = clamp(out, zMin, zMax);
                        }
                    }
                }
            }

            z += inc;
        }

        // This is a silly hack to allow the lambda to mutate the return value.
        double[] ugly = new double[]{out};

        stream.forEach((shape) -> ugly[0] = shape.method_1108(zAxis, box, ugly[0]));

        return ugly[0];
    }

    /**
     * Clone of {@link VoxelShapes#method_17943}, could use an accessor
     */
    private static int clamp(double value, double min, double max) {
        return value > 0.0D ? MathHelper.floor(max + value) + 1 : MathHelper.floor(min + value) - 1;
    }
}
