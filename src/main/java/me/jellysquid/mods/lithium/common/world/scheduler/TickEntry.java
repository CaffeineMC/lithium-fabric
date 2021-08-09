package me.jellysquid.mods.lithium.common.world.scheduler;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ScheduledTick;
import net.minecraft.world.TickPriority;

/**
 * A wrapper type for {@link ScheduledTick} which adds fields to mark the state of the tick in the scheduler's pipeline.
 */
public class TickEntry<T> extends ScheduledTick<T> {
    /**
     * True if the tick has been executed (and therefore consumed). This flag is set right before the tick is actually
     * performed. If this tick is re-scheduled during execution, the consumed flag will be unset and the scheduled
     * flag will be set instead.
     */
    public boolean consumed = false;

    public TickEntry(BlockPos pos, T object, long time, TickPriority priority) {
        super(pos, object, time, priority);
    }

    public TickEntry(BlockPos pos, T t) {
        super(pos, t, 0L, TickPriority.NORMAL);
    }
}

