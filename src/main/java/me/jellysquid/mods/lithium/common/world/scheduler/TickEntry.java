package me.jellysquid.mods.lithium.common.world.scheduler;

import net.minecraft.world.ScheduledTick;

import java.util.Set;

/**
 * A wrapper type for {@link ScheduledTick} which adds fields to mark the state of the tick in the scheduler's pipeline.
 */
public class TickEntry<T> extends ScheduledTick<T> {
    /**
     * True if the tick has been scheduled for execution. After the tick has been selected for execution during the
     * current world tick, this is unset and the executing flag is set. The tick can then no longer be re-scheduled
     * without creating a new tick.
     */
    public boolean scheduled = false;

    /**
     * True if the tick will be executed during the current world tick. After all enqueued ticks have been executed
     * in the tick phase, this is unset. Used by redstone contraptions to know whether or not something is presently
     * being ticked as to avoid duplicate updates.
     */
    public boolean executing = false;

    /**
     * True if the tick has been executed (and therefore consumed). This flag is set right before the tick is actually
     * performed. If this tick is re-scheduled during execution, the consumed flag will be unset and the scheduled
     * flag will be set instead.
     */
    public boolean consumed = false;

    /**
     * A pointer to the chunk index belonging to this scheduled tick.
     */
    public final Set<TickEntry<T>> chunkIdx;

    public TickEntry(ScheduledTick<T> tick, Set<TickEntry<T>> chunkIdx) {
        super(tick.pos, tick.getObject(), tick.time, tick.priority);

        this.chunkIdx = chunkIdx;
        this.chunkIdx.add(this);
    }
}

