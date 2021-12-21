package me.jellysquid.mods.lithium.mixin.math.fast_round;

import me.jellysquid.mods.lithium.common.util.math.FastMath;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SurfaceBuilder.class)
public class SurfaceBuilderMixin {

    @Redirect(
            method = "getTerracottaBlock(III)Lnet/minecraft/block/BlockState;",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Math;round(D)J"
            )
    )
    private long fasterRound(double value) {
        return FastMath.round(value);
    }
}
