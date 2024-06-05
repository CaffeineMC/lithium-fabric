package me.jellysquid.mods.lithium.common.ai.brain;

import me.jellysquid.mods.lithium.mixin.ai.useless_sensors.BrainAccessor;
import me.jellysquid.mods.lithium.mixin.ai.useless_sensors.SensorAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.server.world.ServerWorld;

public class SensorHelper {

    public static void disableSensor(LivingEntity brainedEntity, SensorType<?> sensorType) {
        if (brainedEntity.getWorld().isClient()) {
            return;
        }
        Brain<?> brain = brainedEntity.getBrain();
        Sensor<?> sensor = ((BrainAccessor<?>) brain).getSensors().get(sensorType);
        if (sensor instanceof SensorAccessor sensorAccessor) {
            //Disable the sensor by setting the maximum last sense time, which will make it count down almost forever
            // Removing the whole sensor could be an issue, since it may be serialized and used in a future version.

            //Instead of setting to Long.MAX_VALUE, we want to be able to recover the random offset of the sensor:
            long lastSenseTime = sensorAccessor.getLastSenseTime();
            int senseInterval = sensorAccessor.getSenseInterval(); //Usual values: 20,40,80,200

            long maxMultipleOfSenseInterval = Long.MAX_VALUE - (Long.MAX_VALUE % senseInterval);
            maxMultipleOfSenseInterval -= senseInterval;
            maxMultipleOfSenseInterval += lastSenseTime;

            sensorAccessor.setLastSenseTime(maxMultipleOfSenseInterval);
        }
    }

    public static <T extends LivingEntity, U extends Sensor<T>> void enableSensor(T brainedEntity, SensorType<U> sensorType) {
        enableSensor(brainedEntity, sensorType, false);
    }

    public static <T extends LivingEntity, U extends Sensor<T>> void enableSensor(T brainedEntity, SensorType<U> sensorType, boolean extraTick) {
        if (brainedEntity.getWorld().isClient()) {
            return;
        }

        Brain<?> brain = brainedEntity.getBrain();
        //noinspection unchecked
        U sensor = (U) ((BrainAccessor<?>) brain).getSensors().get(sensorType);
        if (sensor instanceof SensorAccessor sensorAccessor) {
            long lastSenseTime = sensorAccessor.getLastSenseTime();
            int senseInterval = sensorAccessor.getSenseInterval();

            //Recover the random offset of the sensor:
            if (lastSenseTime > senseInterval) {
                lastSenseTime = lastSenseTime % senseInterval;
                if (extraTick) {
                    ((SensorAccessor) sensor).setLastSenseTime(0L);
                    sensor.tick((ServerWorld) brainedEntity.getWorld(), brainedEntity);
                }
            }
            sensorAccessor.setLastSenseTime(lastSenseTime);
        }
    }
}
