package me.jellysquid.mods.lithium.mixin.math.fast_round;

import me.jellysquid.mods.lithium.common.util.math.FastMath;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Redirect(
            method = "increaseTravelMotionStats(DDD)V",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Math;round(D)J"
            )
    )
    private long fasterRound(double value) {
        return FastMath.round(value);
    }


    @Redirect(
            method = "handleFallDamage(FFLnet/minecraft/entity/damage/DamageSource;)Z",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Math;round(D)J"
            )
    )
    private long fasterRoundFall(double value) {
        return FastMath.round(value);
    }
}
