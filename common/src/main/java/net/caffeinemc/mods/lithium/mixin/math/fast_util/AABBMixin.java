package net.caffeinemc.mods.lithium.mixin.math.fast_util;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AABB.class)
public class AABBMixin {
    static {
        assert Direction.Axis.X.ordinal() == 0;
        assert Direction.Axis.Y.ordinal() == 1;
        assert Direction.Axis.Z.ordinal() == 2;
        assert Direction.Axis.values().length == 3;
    }

    @Shadow
    @Final
    public double minX;

    @Shadow
    @Final
    public double minY;

    @Shadow
    @Final
    public double minZ;

    @Shadow
    @Final
    public double maxX;

    @Shadow
    @Final
    public double maxY;

    @Shadow
    @Final
    public double maxZ;

    /**
     * @reason Simplify the code to better help the JVM optimize it
     * @author JellySquid
     */
    @Overwrite
    public double min(Direction.Axis axis) {
        switch (axis.ordinal()) {
            case 0: //X
                return this.minX;
            case 1: //Y
                return this.minY;
            case 2: //Z
                return this.minZ;
        }

        throw new IllegalArgumentException();
    }

    /**
     * @reason Simplify the code to better help the JVM optimize it
     * @author JellySquid
     */
    @Overwrite
    public double max(Direction.Axis axis) {
        switch (axis.ordinal()) {
            case 0: //X
                return this.maxX;
            case 1: //Y
                return this.maxY;
            case 2: //Z
                return this.maxZ;
        }

        throw new IllegalArgumentException();

    }
}
