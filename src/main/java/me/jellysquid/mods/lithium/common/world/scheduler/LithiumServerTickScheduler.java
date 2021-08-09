package me.jellysquid.mods.lithium.common.world.scheduler;

import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.jellysquid.mods.lithium.common.util.Pos;
import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ScheduledTick;
import net.minecraft.world.TickPriority;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Provides greatly improved performance when compared to the vanilla tick scheduler. Key highlights:
 * - Instead of using a TreeSet collection (which is generally very slow, relatively speaking) for ordering updates, we
 * make use of bucketed array queues indexed by a unique key composed of the scheduled time and priority. When
 * iterating back over these updates, we simply specify the maximum bucket key range to avoid iterating over
 * unnecessary elements. Integer bucket keys are much faster to sort against (as they are logically sorted) and
 * are computationally trivial to slice.
 * <p>
 * - A single single collection is used for storing ticks in the pipeline and execution flags are set on the scheduled
 * objects directly. This eliminates the need to move ticks between multiple queues and sets constantly.
 * <p>
 * - We avoid repeatedly asking if a chunk is available by trying to re-use the previous computation if it involves the
 * same chunk, reducing a lot of map operations elsewhere.
 * <p>
 * - Ticks are stored in a HashMap with their execution state, meaning that redstone gates and other blocks which check
 * to see if something is scheduled/executing will not have to scan a potentially very large array (which can occur
 * when many ticks have been scheduled.)
 * <p>
 * <p>
 * Vanilla Tick Scheduler Behavior Summary
 * ScheduledTicks will to be executed in their corresponding gametick. The ScheduledTicks are executed ordered by their
 * priority (lower first). ScheduledTicks with the same priority are executed in the order they were added to the TickScheduler.
 * ScheduledTicks cannot be added to the TickScheduler if there is already another ScheduledTick added for that position.
 * <p>
 * There are several states a ScheduledTick can be in. A scheduled tick can only be in one state at a time:
 * - scheduled
 *      stored in collection {@link #scheduledTicks}
 *      {@link #isScheduled(BlockPos, Object)} returns true,
 *      adds to count in {@link #getTicks()}, prevents scheduling another tick in the same location for the same type
 * <p>
 * - selected for execution
 *      stored in collection {@link #executingTicks}, not marked as consumed
 *      {@link #isTicking(BlockPos, Object)} returns true
 *
 * - executing or done executing
 *      stored in collection {@link #executingTicks} but marked as consumed
 *      {@link #isTicking(BlockPos, Object)} returns false
 */
public class LithiumServerTickScheduler<T> extends ServerTickScheduler<T> {
    //These two collections store the same ScheduledTicks and must stay consistent
    private final Long2ObjectSortedMap<TickEntryQueue<T>> scheduledTicksOrdered = new Long2ObjectAVLTreeMap<>();
    private final ObjectOpenHashSet<TickEntry<T>> scheduledTicks = new ObjectOpenHashSet<>();

    //These two collections store the same ScheduledTicks and must stay consistent
    private final ArrayList<TickEntry<T>> executingTicks = new ArrayList<>();
    private final ObjectOpenHashSet<TickEntry<T>> executingTicksSet = new ObjectOpenHashSet<>();

    private final Predicate<T> invalidObjPredicate;
    private final ServerWorld world;
    private final Consumer<ScheduledTick<T>> tickConsumer;

    public LithiumServerTickScheduler(ServerWorld world, Predicate<T> invalidPredicate, Function<T, Identifier> idToName, Consumer<ScheduledTick<T>> tickConsumer) {
        super(world, invalidPredicate, idToName, tickConsumer);

        this.invalidObjPredicate = invalidPredicate;
        this.world = world;
        this.tickConsumer = tickConsumer;
    }

    @Override
    public void tick() {
        this.world.getProfiler().push("cleaning");

        this.selectTicks(this.world.getTime());

        this.world.getProfiler().swap("executing");

        this.executeTicks(this.tickConsumer);

        this.world.getProfiler().pop();
    }

    @Override
    public boolean isTicking(BlockPos pos, T obj) {
        TickEntry<T> entry = this.executingTicksSet.get(new TickEntry<>(pos, obj));

        if (entry == null) {
            return false;
        }

        return !entry.consumed;
    }

    @Override
    public boolean isScheduled(BlockPos pos, T obj) {
        return this.scheduledTicks.contains(new TickEntry<>(pos, obj));
    }

    @Override
    public List<ScheduledTick<T>> getScheduledTicks(BlockBox bounds, boolean remove, boolean getConsumedTicks) {
        ArrayList<ScheduledTick<T>> collectedTicks = new ArrayList<>();
        ObjectOpenHashSet<TickEntry<T>> scheduledTicks = this.scheduledTicks;
        for (TickEntryQueue<T> tickEntryQueue : this.scheduledTicksOrdered.values()) {
            for (int i = 0; i < tickEntryQueue.size(); i++) {
                TickEntry<T> tick = tickEntryQueue.getTickAtIndex(i);
                if (tick == null) {
                    continue;
                }
                BlockPos tickPos = tick.pos;
                // The minimum coordinate is inclusive while the maximum coordinate is exclusive
                if (tickPos.getX() >= bounds.getMinX() && tickPos.getX() < bounds.getMaxX() && tickPos.getZ() >= bounds.getMinZ() && tickPos.getZ() < bounds.getMaxZ()) {
                    collectedTicks.add(new TickEntry<>(tick.pos, tick.getObject(), tick.time, tick.priority));
                    if (remove) {
                        tickEntryQueue.setTickAtIndex(i, null);
                        scheduledTicks.remove(tick);
                    }
                }
            }
        }

        if (!this.executingTicks.isEmpty()) {
            ArrayList<ScheduledTick<T>> consumedTicks = new ArrayList<>();
            ArrayList<TickEntry<T>> executingTicks = this.executingTicks;
            ObjectOpenHashSet<TickEntry<T>> executingTicksSet = this.executingTicksSet;
            for (int i = 0, ticksSize = executingTicks.size(); i < ticksSize; i++) {
                TickEntry<T> tick = executingTicks.get(i);
                if (tick == null || (!getConsumedTicks && tick.consumed)) {
                    continue;
                }
                BlockPos tickPos = tick.pos;
                // The minimum coordinate is inclusive while the maximum coordinate is exclusive
                if (tickPos.getX() >= bounds.getMinX() && tickPos.getX() < bounds.getMaxX() && tickPos.getZ() >= bounds.getMinZ() && tickPos.getZ() < bounds.getMaxZ()) {
                    (tick.consumed ? consumedTicks : collectedTicks).add(new TickEntry<>(tick.pos, tick.getObject(), tick.time, tick.priority));

                    if (remove) {
                        executingTicks.set(i, null);
                        executingTicksSet.remove(tick);
                    }
                }
            }
            // [VanillaCopy] order of ScheduledTicks: consumed ticks after executing ticks
            collectedTicks.addAll(consumedTicks);
        }
        return collectedTicks;
    }

    // Computes a timestamped key including the tick's priority
    // Keys can be sorted in descending order to find what should be executed first
    // 60 time bits, 4 priority bits
    private static long getBucketKey(long time, TickPriority priority) {
        //using priority.ordinal() as is not negative instead of priority.index
        return (time << 4L) | (priority.ordinal() & 15);
    }

    @Override
    public void copyScheduledTicks(BlockBox box, BlockPos pos) {
        List<ScheduledTick<T>> list = this.getScheduledTicks(box, false, false);

        for (ScheduledTick<T> tick : list) {
            this.scheduleTick(tick.pos.add(pos), tick.getObject(), tick.time, tick.priority);
        }
    }

    /**
     * Returns the number of currently scheduled ticks.
     */
    @Override
    public int getTicks() {
        return this.scheduledTicks.size();
    }

    @Override
    public void schedule(BlockPos pos, T obj, int delay, TickPriority priority) {
        if (!this.invalidObjPredicate.test(obj)) {
            this.scheduleTick(pos, obj, (long) delay + this.world.getTime(), priority);
        }
    }

    /**
     * Enqueues all scheduled ticks before the specified time and prepares them for execution.
     */
    public void selectTicks(long time) {
        // Calculates the maximum key value which includes all ticks scheduled before the specified time
        long headKey = getBucketKey(time + 1, TickPriority.EXTREMELY_HIGH) - 1;

        // [VanillaCopy] ServerTickScheduler#tick
        // In order to fulfill the promise of not breaking vanilla behaviour, we keep the vanilla artifact of
        // tick suppression.
        int limit = 65536;

        boolean canTick = true;
        long prevChunk = Long.MIN_VALUE;

        // Create an iterator over only
        Iterator<TickEntryQueue<T>> it = this.scheduledTicksOrdered.headMap(headKey).values().iterator();

        // Iterate over all scheduled ticks and enqueue them for until we exceed our budget
        while (limit > 0 && it.hasNext()) {
            TickEntryQueue<T> list = it.next();

            // Pointer for writing scheduled ticks back into the queue
            int w = 0;

            // Re-builds the scheduled tick queue in-place
            for (int i = 0; i < list.size(); i++) {
                TickEntry<T> tick = list.getTickAtIndex(i);

                if (tick == null) {
                    continue;
                }

                // If no more ticks can be scheduled for execution this phase, then we leave it in its current time
                // bucket and skip it. This deliberately introduces a bug where backlogged ticks will not be re-scheduled
                // properly, re-producing the vanilla issue of tick suppression.
                if (limit > 0) {
                    long chunk = ChunkPos.toLong(Pos.ChunkCoord.fromBlockCoord(tick.pos.getX()), Pos.ChunkCoord.fromBlockCoord(tick.pos.getZ()));

                    // Take advantage of the fact that if any position in a chunk can be updated, then all other positions
                    // in the same chunk can be updated. This avoids the more expensive check to the chunk manager.
                    if (prevChunk != chunk) {
                        prevChunk = chunk;
                        canTick = this.world.method_37117(tick.pos);
                    }

                    // If the tick can be executed right now, then add it to the executing list and decrement our
                    // budget limit.
                    if (canTick) {
                        this.scheduledTicks.remove(tick);
                        this.selectForExecution(tick);
                        limit--;

                        // Avoids the tick being kept in the scheduled queue
                        continue;
                    }
                }

                // Nothing happened to this tick, so re-add it to the queue
                list.setTickAtIndex(w++, tick);
            }

            // Finalize our changes to the queue and notify it of the new length
            list.resize(w);

            // If the queue is empty, remove it from the map
            if (list.isEmpty()) {
                it.remove();
            }
        }
    }

    public void executeTicks(Consumer<ScheduledTick<T>> consumer) {
        TickEntry<T> tick = null;
        try {
            // Execute all executing ticks
            ArrayList<TickEntry<T>> ticks = this.executingTicks;
            for (int i = 0, ticksSize = ticks.size(); i < ticksSize; i++) {
                tick = ticks.get(i);
                if (tick == null) {
                    continue;
                }
                // Mark as consumed before execution per vanilla behaviour
                tick.consumed = true;

                // Perform tick execution
                consumer.accept(tick);

            }
        } catch (Throwable e) {
            CrashReport crash = CrashReport.create(e, "Exception while ticking");
            CrashReportSection section = crash.addElement("Block being ticked");
            CrashReportSection.addBlockInfo(section, this.world, tick == null ? BlockPos.ORIGIN : tick.pos, null);

            throw new CrashException(crash);
        }


        // We finished executing those ticks, so empty the list.
        this.executingTicks.clear();
        this.executingTicksSet.clear();
    }

    /**
     * Schedules a tick for execution if it has not already been. To match vanilla, we do not re-schedule matching
     * scheduled ticks which are set to execute at a different time.
     */
    private void scheduleTick(BlockPos pos, T object, long time, TickPriority priority) {
        TickEntry<T> tick = new TickEntry<>(pos, object, time, priority);
        boolean added = this.scheduledTicks.add(tick);
        if (added) {
            TickEntryQueue<T> timeIdx = this.scheduledTicksOrdered.computeIfAbsent(getBucketKey(tick.time, tick.priority), key -> new TickEntryQueue<>());
            timeIdx.push(tick);
        }
    }

    private void selectForExecution(TickEntry<T> tick) {
        this.executingTicks.add(tick);
        this.executingTicksSet.add(tick);
    }
}