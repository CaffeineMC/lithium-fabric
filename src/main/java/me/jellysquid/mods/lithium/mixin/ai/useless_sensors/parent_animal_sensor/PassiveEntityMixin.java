package me.jellysquid.mods.lithium.mixin.ai.useless_sensors.parent_animal_sensor;

import me.jellysquid.mods.lithium.common.ai.brain.SensorHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(PassiveEntity.class)
public abstract class PassiveEntityMixin extends LivingEntity {

    @Shadow
    public abstract boolean isBaby();

    protected PassiveEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(
            method = "onTrackedDataSet",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/PassiveEntity;calculateDimensions()V"),
            require = 1, allow = 1
    )
    private void handleParentSensor(TrackedData<?> data, CallbackInfo ci) {
        if (this.getWorld().isClient()) {
            return;
        }
        if (isBaby()) {
            SensorHelper.enableSensor((PassiveEntity) (Object) this, SensorType.NEAREST_ADULT, true);
        } else {
            SensorHelper.disableSensor((PassiveEntity) (Object) this, SensorType.NEAREST_ADULT);
            if (this.getBrain().hasMemoryModule(MemoryModuleType.NEAREST_VISIBLE_ADULT)) {
                this.getBrain().remember(MemoryModuleType.NEAREST_VISIBLE_ADULT, Optional.empty());
            }
        }
    }
}
