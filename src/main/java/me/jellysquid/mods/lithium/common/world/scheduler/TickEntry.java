package me.jellysquid.mods.lithium.common.world.scheduler;

import net.minecraft.world.ScheduledTick;

/**
 * A wrapper type for {@link ScheduledTick} which adds fields to mark the state of the tick in the scheduler's pipeline.
 */
public class TickEntry<T> extends ScheduledTick<T> {
    public boolean scheduled = false;
    public boolean executing = false;
    public boolean consumed = false;

    public TickEntry(ScheduledTick<T> tick) {
        super(tick.pos, tick.getObject(), tick.time, tick.priority);
    }
}

