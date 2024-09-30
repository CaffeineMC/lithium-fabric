package me.jellysquid.mods.lithium.mixin.ai.useless_sensors.goat_item_sensor;

import me.jellysquid.mods.lithium.common.ai.brain.SensorHelper;
import net.minecraft.SharedConstants;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Goat.class)
public abstract class GoatEntityMixin extends LivingEntity {

    protected GoatEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Shadow
    public abstract Brain<Goat> getBrain();

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void disableItemSensor(CallbackInfo ci) {
        if (!this.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM)) {
            SensorHelper.disableSensor(this, SensorType.NEAREST_ITEMS);
        } else if (SharedConstants.IS_RUNNING_IN_IDE) {
            throw new IllegalStateException("Goat Entity has a nearest visible wanted item memory module! The mixin.ai.useless_sensors.goat_item_sensor should probably be removed permanently!");
        }
    }
}
