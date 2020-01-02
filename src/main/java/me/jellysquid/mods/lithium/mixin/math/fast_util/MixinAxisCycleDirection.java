package me.jellysquid.mods.lithium.mixin.math.fast_util;

import net.minecraft.util.math.AxisCycleDirection;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(AxisCycleDirection.class)
public class MixinAxisCycleDirection {
    @Mixin(targets = "net/minecraft/util/math/AxisCycleDirection$2")
    public static class MixinForward {
        /**
         * @reason Avoid expensive array/modulo operations
         * @author JellySquid
         */
        @Overwrite
        public Direction.Axis cycle(Direction.Axis axis) {
            switch (axis) {
                case X:
                    return Direction.Axis.Y;
                case Y:
                    return Direction.Axis.Z;
                case Z:
                    return Direction.Axis.X;
            }

            throw new IllegalArgumentException();
        }
    }

    @Mixin(targets = "net/minecraft/util/math/AxisCycleDirection$3")
    public static class MixinBackward {
        /**
         * @reason Avoid expensive array/modulo operations
         * @author JellySquid
         */
        @Overwrite
        public Direction.Axis cycle(Direction.Axis axis) {
            switch (axis) {
                case X:
                    return Direction.Axis.Z;
                case Y:
                    return Direction.Axis.X;
                case Z:
                    return Direction.Axis.Y;
            }

            throw new IllegalArgumentException();
        }
    }
}
