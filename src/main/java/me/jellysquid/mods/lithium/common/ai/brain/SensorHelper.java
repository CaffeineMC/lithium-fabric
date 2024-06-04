package me.jellysquid.mods.lithium.common.ai.brain;

import me.jellysquid.mods.lithium.mixin.ai.useless_sensors.BrainAccessor;
import me.jellysquid.mods.lithium.mixin.ai.useless_sensors.SensorAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;

public class SensorHelper {

    public static void disableSensor(LivingEntity brainedEntity, SensorType<?> sensorType) {
        Brain<?> brain = brainedEntity.getBrain();
        Sensor<?> sensor = ((BrainAccessor<?>) brain).getSensors().get(sensorType);
        if (sensor instanceof SensorAccessor sensorAccessor) {
            //Disable the sensor by setting the maximum last sense time, which will make it count down almost forever
            // Removing the whole sensor could be an issue, since it may be serialized and used in a future version.
            sensorAccessor.setLastSenseTime(Long.MAX_VALUE);
        }
    }
}
