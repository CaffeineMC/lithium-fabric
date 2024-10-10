package net.caffeinemc.mods.lithium.mixin.math.fast_blockpos;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The vector of each Direction is usually stored inside another object, which introduces indirection and makes things
 * harder for the JVM to optimize. This patch simply hoists the offset fields to the Direction enum itself.
 */
@Mixin(Direction.class)
public class DirectionMixin {
    private int offsetX, offsetY, offsetZ;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void reinit(String enumName, int ordinal, int id, int idOpposite, int idHorizontal, String name, Direction.AxisDirection direction, Direction.Axis axis, Vec3i vector, CallbackInfo ci) {
        this.offsetX = vector.getX();
        this.offsetY = vector.getY();
        this.offsetZ = vector.getZ();
    }

    /**
     * @reason Avoid indirection to aid inlining
     * @author JellySquid
     */
    @Overwrite
    public int getStepX() {
        return this.offsetX;
    }

    /**
     * @reason Avoid indirection to aid inlining
     * @author JellySquid
     */
    @Overwrite
    public int getStepY() {
        return this.offsetY;
    }

    /**
     * @reason Avoid indirection to aid inlining
     * @author JellySquid
     */
    @Overwrite
    public int getStepZ() {
        return this.offsetZ;
    }
}
