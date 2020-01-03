package me.jellysquid.mods.lithium.mixin.math.fast_util;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Box.class)
public class MixinBox {
    @Shadow
    @Final
    public double x1;

    @Shadow
    @Final
    public double y1;

    @Shadow
    @Final
    public double y2;

    @Shadow
    @Final
    public double z1;

    @Shadow
    @Final
    public double x2;

    @Shadow
    @Final
    public double z2;

    /**
     * @reason Simplify the code to better help the JVM optimize it
     * @author JellySquid
     */
    @Overwrite
    public double getMin(Direction.Axis axis) {
        switch (axis) {
            case X:
                return this.x1;
            case Y:
                return this.y1;
            case Z:
                return this.z1;
        }

        throw new IllegalArgumentException();
    }

    /**
     * @reason Simplify the code to better help the JVM optimize it
     * @author JellySquid
     */
    @Overwrite
    public double getMax(Direction.Axis axis) {
        switch (axis) {
            case X:
                return this.x2;
            case Y:
                return this.y2;
            case Z:
                return this.z2;
        }

        throw new IllegalArgumentException();

    }
}
