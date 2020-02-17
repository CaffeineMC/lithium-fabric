package me.jellysquid.mods.lithium.common.world.scheduler;

import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMap;
import net.minecraft.server.world.ServerChunkManager;
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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Provides greatly improved performance when compared to the vanilla tick scheduler. Key highlights:
 * - Instead of using a TreeSet collection (which is generally very slow, relatively speaking) for ordering updates, we
 *   make use of bucketed array queues indexed by a unique key composed of the scheduled time and priority. When
 *   iterating back over these updates, we simply specify the maximum bucket key range to avoid iterating over
 *   unnecessary elements. Integer bucket keys are much faster to sort against (as they are logically sorted) and
 *   are computationally trivial to slice.
 *
 * - A single single collection is used for storing ticks in the pipeline and execution flags are set on the scheduled
 *   objects directly. This eliminates the need to move ticks between multiple queues and sets constantly.
 *
 * - We avoid repeatedly asking if a chunk is available by trying to re-use the previous computation if it involves the
 *   same chunk, reducing a lot of map operations elsewhere.
 *
 * - Ticks are stored in a HashMap with their execution state, meaning that redstone gates and other blocks which check
 *   to see if something is scheduled/executing will not have to scan a potentially very large array (which can occur
 *   when many ticks have been scheduled.)
 */
public class LithiumServerTickScheduler<T> extends ServerTickScheduler<T> {
    private static final Predicate<TickEntry<?>> PREDICATE_ANY_TICK = entry -> true;
    private static final Predicate<TickEntry<?>> PREDICATE_ACTIVE_TICKS = entry -> !entry.consumed;

    private final Long2ObjectSortedMap<TickEntryQueue<T>> scheduledTicksOrdered = new Long2ObjectAVLTreeMap<>();
    private final Map<ScheduledTick<T>, TickEntry<T>> scheduledTicks = new HashMap<>();
    private final ArrayList<TickEntry<T>> executingTicks = new ArrayList<>();

    private final Predicate<T> invalidObjPredicate;
    private final ServerWorld world;
    private final Consumer<ScheduledTick<T>> tickConsumer;

    public LithiumServerTickScheduler(ServerWorld world, Predicate<T> invalidPredicate, Function<T, Identifier> idToName, Function<Identifier, T> nameToId, Consumer<ScheduledTick<T>> consumer) {
        super(world, invalidPredicate, idToName, nameToId, consumer);

        this.invalidObjPredicate = invalidPredicate;
        this.world = world;
        this.tickConsumer = consumer;
    }

    @Override
    public void tick() {
        this.world.getProfiler().push("cleaning");

        this.selectTicks(this.world.getChunkManager(), this.world.getTime());

        this.world.getProfiler().swap("executing");

        this.executeTicks(this.tickConsumer);

        this.world.getProfiler().pop();
    }

    @Override
    public boolean isTicking(BlockPos pos, T obj) {
        TickEntry<T> entry = this.scheduledTicks.get(new ScheduledTick<>(pos, obj));

        if (entry == null) {
            return false;
        }

        return entry.executing;
    }

    @Override
    public boolean isScheduled(BlockPos pos, T obj) {
        TickEntry<T> entry = this.scheduledTicks.get(new ScheduledTick<>(pos, obj));

        if (entry == null) {
            return false;
        }

        return entry.scheduled;
    }

    @Override
    public void scheduleAll(Stream<ScheduledTick<T>> stream) {
        stream.forEach(this::addScheduledTick);
    }

    @Override
    public List<ScheduledTick<T>> getScheduledTicksInChunk(ChunkPos chunkPos, boolean mutates, boolean getStaleTicks) {
        BlockBox box = new BlockBox(chunkPos.getStartX() - 2, chunkPos.getStartZ() - 2, chunkPos.getEndX() + 2, chunkPos.getEndZ() + 2);

        return this.getScheduledTicks(box, mutates, getStaleTicks);
    }

    @Override
    public List<ScheduledTick<T>> getScheduledTicks(BlockBox box, boolean remove, boolean getStaleTicks) {
        Predicate<TickEntry<?>> predicate = getStaleTicks ? PREDICATE_ANY_TICK : PREDICATE_ACTIVE_TICKS;

        return remove ? this.removeTicks(box, predicate) : this.collectTicks(box, predicate);
    }

    @Override
    public void copyScheduledTicks(BlockBox box, BlockPos pos) {
        List<ScheduledTick<T>> list = this.getScheduledTicks(box, false, false);

        for (ScheduledTick<T> tick : list) {
            this.addScheduledTick(new ScheduledTick<>(tick.pos.add(pos), tick.getObject(), tick.time, tick.priority));
        }
    }

    @Override
    public void schedule(BlockPos pos, T obj, int delay, TickPriority priority) {
        if (!this.invalidObjPredicate.test(obj)) {
            this.addScheduledTick(new ScheduledTick<>(pos, obj, (long) delay + this.world.getTime(), priority));
        }
    }

    /**
     * Returns the number of currently scheduled ticks.
     */
    @Override
    public int method_20825() {
        int count = 0;

        for (TickEntry<T> entry : this.scheduledTicks.values()) {
            if (entry.scheduled) {
                count += 1;
            }
        }

        return count;
    }

    /**
     * Enqueues all scheduled ticks before the specified time and prepares them for execution.
     */
    public void selectTicks(ServerChunkManager chunkManager, long time) {
        // Calculates the maximum key value which includes all ticks scheduled before the specified time
        long headKey = getBucketKey(time + 1, TickPriority.EXTREMELY_HIGH) - 1;

        // [VanillaCopy] ServerTickScheduler#tick
        // In order to fulfill the promise of not breaking vanilla behaviour, we keep the vanilla artifact of
        // tick suppression.
        int limit = 65565;

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

                if (!tick.scheduled) {
                    continue;
                }

                // If no more ticks can be scheduled for execution this phase, then we leave it in its current time
                // bucket and skip it. This deliberately introduces a bug where backlogged ticks will not be re-scheduled
                // properly, re-producing the vanilla issue of tick suppression.
                if (limit > 0) {
                    long chunk = ChunkPos.toLong(tick.pos.getX() >> 4, tick.pos.getZ() >> 4);

                    // Take advantage of the fact that if any position in a chunk can be updated, then all other positions
                    // in the same chunk can be updated. This avoids the more expensive check to the chunk manager.
                    if (prevChunk != chunk) {
                        prevChunk = chunk;
                        canTick = chunkManager.shouldTickBlock(tick.pos);
                    }

                    // If the tick can be executed right now, then add it to the executing list and decrement our
                    // budget limit.
                    if (canTick) {
                        tick.scheduled = false;
                        tick.executing = true;

                        this.executingTicks.add(tick);

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
        // Mark and execute all executing ticks
        for (TickEntry<T> entry : this.executingTicks) {
            try {
                // Mark as consumed before execution per vanilla behaviour
                entry.consumed = true;
                entry.executing = false;

                // Perform tick execution
                consumer.accept(entry);

                // If the tick was re-scheduled during execution, then unset the consumed flag to prepare it for
                // re-scheduling.
                if (entry.scheduled) {
                    entry.consumed = false;
                } else {
                    // The tick was not re-scheduled, so it should be destroyed
                    this.scheduledTicks.remove(entry);
                }
            } catch (Throwable e) {
                CrashReport crash = CrashReport.create(e, "Exception while ticking");
                CrashReportSection section = crash.addElement("Block being ticked");
                CrashReportSection.addBlockInfo(section, entry.pos, null);

                throw new CrashException(crash);
            }
        }


        // We finished executing those ticks, so empty the list.
        this.executingTicks.clear();
    }


    /**
     * Returns a list containing all scheduled ticks in the specified {@param box} matching the {@param predicate}.
     */
    private List<ScheduledTick<T>> collectTicks(BlockBox box, Predicate<TickEntry<?>> predicate) {
        List<ScheduledTick<T>> ret = new ArrayList<>();

        for (TickEntryQueue<T> ticks : this.scheduledTicksOrdered.values()) {
            for (int i = 0; i < ticks.size(); i++) {
                TickEntry<T> tick = ticks.getTickAtIndex(i);

                if (box.contains(tick.pos) && predicate.test(tick)) {
                    ret.add(tick);
                }
            }
        }

        return ret;
    }

    /**
     * Variant of {@link LithiumServerTickScheduler#collectTicks(BlockBox, Predicate)}, but collected ticks are removed
     * from the scheduler.
     */
    private List<ScheduledTick<T>> removeTicks(BlockBox box, Predicate<TickEntry<?>> predicate) {
        List<ScheduledTick<T>> ret = new ArrayList<>();

        // TODO: Do not update all lists if no changes are made
        for (Iterator<TickEntryQueue<T>> timeIdxIt = this.scheduledTicksOrdered.values().iterator(); timeIdxIt.hasNext(); ) {
            TickEntryQueue<T> list = timeIdxIt.next();

            // Pointer for writing scheduled ticks back into the queue
            int w = 0;

            // Perform an in-place modification of the queue
            for (int i = 0; i < list.size(); i++) {
                TickEntry<T> tick = list.getTickAtIndex(i);

                // If the predicates do not match this tick, add it back to the queue and continue
                if (!box.contains(tick.pos) || !predicate.test(tick)) {
                    list.setTickAtIndex(w++, tick);

                    continue;
                }

                // If we remove a scheduled tick, this flag needs to be unset so other things know it will never be
                // scheduled again without a call back to this class.
                tick.scheduled = false;

                // Add the tick to the returned list
                ret.add(tick);
            }

            // Finalize our changes to the queue and notify it of the new length
            list.resize(w);

            // The queue is now empty, so we should remove it to prevent it being considered in the future.
            if (list.isEmpty()) {
                timeIdxIt.remove();
            }
        }

        return ret;
    }

    /**
     * Schedules a tick for execution if it has not already been. To match vanilla, we do not re-schedule matching
     * scheduled ticks which are set to execute at a different time.
     */
    private void addScheduledTick(ScheduledTick<T> tick) {
        TickEntry<T> entry = this.scheduledTicks.computeIfAbsent(tick, TickEntry::new);

        if (!entry.scheduled) {
            TickEntryQueue<T> timeIdx = this.scheduledTicksOrdered.computeIfAbsent(getBucketKey(tick.time, tick.priority), key -> new TickEntryQueue<>());
            timeIdx.push(entry);

            entry.scheduled = true;
        }
    }

    // Computes a timestamped key including the tick's priority
    // Keys can be sorted in descending order to find what should be executed first
    // 60 time bits, 4 priority bits
    private static long getBucketKey(long time, TickPriority priority) {
        return (time << 4L) | (priority.ordinal() & 15);
    }
}