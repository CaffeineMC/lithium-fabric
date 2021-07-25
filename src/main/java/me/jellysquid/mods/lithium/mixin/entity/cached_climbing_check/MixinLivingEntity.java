package me.jellysquid.mods.lithium.mixin.entity.cached_climbing_check;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {
    @Redirect(method = "isPushable", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isClimbing()Z"))
    boolean preventClimbingUpdateCheck(LivingEntity livingEntity) {
        return livingEntity.getClimbingPos() != Optional.<BlockPos>empty() || livingEntity.isClimbing();
    }
}
