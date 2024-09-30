package net.caffeinemc.mods.lithium.mixin.minimal_nonvanilla.ai.sensor.frog_attackables;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.FrogAttackablesSensor;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.animal.frog.Frog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FrogAttackablesSensor.class)
public class FrogAttackablesSensorMixin {

    @Redirect(
            method = "isMatchingEntity(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/sensing/Sensor;isEntityAttackable(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z")
    )
    private boolean checkTypeAndPredicate(LivingEntity entity, LivingEntity target) {
        return Frog.canEat(target) && Sensor.isEntityAttackable(entity, target);
    }

    @Redirect(
            method = "isMatchingEntity(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/frog/Frog;canEat(Lnet/minecraft/world/entity/LivingEntity;)Z"),
            require = 0
    )
    private boolean skipCheckTypeAgain(LivingEntity entity) {
        return true;
    }
}
