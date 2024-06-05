package me.jellysquid.mods.lithium.mixin.ai.useless_sensors;

import net.minecraft.entity.ai.brain.sensor.Sensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Sensor.class)
public interface SensorAccessor {

    @Accessor("lastSenseTime")
    long getLastSenseTime();

    @Accessor("senseInterval")
    int getSenseInterval();

    @Accessor("lastSenseTime")
    void setLastSenseTime(long lastSenseTime);
}
