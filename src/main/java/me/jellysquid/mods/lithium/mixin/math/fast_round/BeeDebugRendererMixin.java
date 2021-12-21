package me.jellysquid.mods.lithium.mixin.math.fast_round;

import me.jellysquid.mods.lithium.common.util.math.FastMath;
import net.minecraft.client.render.debug.BeeDebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BeeDebugRenderer.class)
public class BeeDebugRendererMixin {

    @Redirect(
            method = "getPositionString(Lnet/minecraft/client/render/debug/BeeDebugRenderer$Bee;Lnet/minecraft/util/math/BlockPos;)Ljava/lang/String;",
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
