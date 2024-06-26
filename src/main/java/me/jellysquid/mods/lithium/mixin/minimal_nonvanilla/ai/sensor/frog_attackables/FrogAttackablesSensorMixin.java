package me.jellysquid.mods.lithium.mixin.minimal_nonvanilla.ai.sensor.frog_attackables;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.sensor.FrogAttackablesSensor;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.passive.FrogEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FrogAttackablesSensor.class)
public class FrogAttackablesSensorMixin {

    @Redirect(
            method = "matches(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/brain/sensor/Sensor;testAttackableTargetPredicate(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z")
    )
    private boolean checkTypeAndPredicate(LivingEntity entity, LivingEntity target) {
        return FrogEntity.isValidFrogFood(target) && Sensor.testAttackableTargetPredicate(entity, target);
    }

    @Redirect(
            method = "matches(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/FrogEntity;isValidFrogFood(Lnet/minecraft/entity/LivingEntity;)Z"),
            require = 0
    )
    private boolean skipCheckTypeAgain(LivingEntity entity) {
        return true;
    }
}
