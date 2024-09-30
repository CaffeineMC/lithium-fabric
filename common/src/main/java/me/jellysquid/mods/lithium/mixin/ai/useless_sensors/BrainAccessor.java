package me.jellysquid.mods.lithium.mixin.ai.useless_sensors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;

@Mixin(Brain.class)
public interface BrainAccessor<E extends LivingEntity> {

    @Accessor("sensors")
    Map<SensorType<? extends Sensor<? super E>>, Sensor<? super E>> getSensors();
}
