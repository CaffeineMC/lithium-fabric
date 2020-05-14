package me.jellysquid.mods.lithium.mixin.math.fast_util;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Box.class)
public class MixinBox {

    static {
        assert Direction.Axis.X.ordinal() == 0;
        assert Direction.Axis.Y.ordinal() == 1;
        assert Direction.Axis.Z.ordinal() == 2;
        assert Direction.Axis.values().length == 3;
    }

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
        switch (axis.ordinal()) {
            case 0: //X
                return this.x1;
            case 1: //Y
                return this.y1;
            case 2: //Z
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
        switch (axis.ordinal()) {
            case 0: //X
                return this.x2;
            case 1: //Y
                return this.y2;
            case 2: //Z
                return this.z2;
        }

        throw new IllegalArgumentException();

    }
}
