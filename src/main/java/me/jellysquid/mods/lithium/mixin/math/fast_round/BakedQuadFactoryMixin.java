package me.jellysquid.mods.lithium.mixin.math.fast_round;

import me.jellysquid.mods.lithium.common.util.math.FastMath;
import net.minecraft.client.render.model.BakedQuadFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BakedQuadFactory.class)
public class BakedQuadFactoryMixin {

    @Redirect(
            method = "uvLock(Lnet/minecraft/client/render/model/json/ModelElementTexture;Lnet/minecraft/util/math/"+
                    "Direction;Lnet/minecraft/util/math/AffineTransformation;Lnet/minecraft/util/Identifier;)"+
                    "Lnet/minecraft/client/render/model/json/ModelElementTexture;",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Math;round(D)J"
            )
    )
    private static long fasterRound(double value) {
        return FastMath.round(value);
    }
}
