package me.jellysquid.mods.lithium.mixin.ai.useless_sensors.goat_item_sensor;

import me.jellysquid.mods.lithium.common.ai.brain.SensorHelper;
import net.minecraft.SharedConstants;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.passive.GoatEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GoatEntity.class)
public abstract class GoatEntityMixin extends LivingEntity {

    protected GoatEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    public abstract Brain<GoatEntity> getBrain();

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void disableItemSensor(CallbackInfo ci) {
        if (!this.getBrain().hasMemoryModule(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM)) {
            SensorHelper.disableSensor(this, SensorType.NEAREST_ITEMS);
        } else if (SharedConstants.isDevelopment) {
            throw new IllegalStateException("Goat Entity has a nearest visible wanted item memory module! The mixin.ai.useless_sensors.goat_item_sensor should probably be removed permanently!");
        }
    }
}
