package me.jellysquid.mods.lithium.mixin.math.fast_round;

import me.jellysquid.mods.lithium.common.util.math.FastMath;
import net.minecraft.util.shape.VoxelShapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = VoxelShapes.class, priority = 1010)
public class VoxelShapesMixin {

    @Redirect(
            method = "cuboidUnchecked(DDDDDD)Lnet/minecraft/util/shape/VoxelShape;",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Math;round(D)J"
            )
    )
    private static long fasterRoundCuboid(double value) {
        return FastMath.round(value);
    }

    @Redirect(
            method = "findRequiredBitResolution(DD)I",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Math;round(D)J"
            )
    )
    private static long fasterRoundResolution(double value) {
        return FastMath.round(value);
    }
}
