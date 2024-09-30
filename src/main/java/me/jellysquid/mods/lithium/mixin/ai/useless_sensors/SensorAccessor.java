package me.jellysquid.mods.lithium.mixin.ai.useless_sensors;

import net.minecraft.world.entity.ai.sensing.Sensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Sensor.class)
public interface SensorAccessor {

    @Accessor("timeToTick")
    long getLastSenseTime();

    @Accessor("scanRate")
    int getSenseInterval();

    @Accessor("timeToTick")
    void setLastSenseTime(long lastSenseTime);
}
