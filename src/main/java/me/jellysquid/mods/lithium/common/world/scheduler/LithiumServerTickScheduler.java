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
 *   unnecessary elements. Integer bucket keys are much faster to sort against and are computationally trivial to slice.
 *
 * - A single single collection is used for storing ticks in the pipeline and execution flags are set on the scheduled
 *   objects directly. This eliminates the need to move ticks between multiple queues and sets constantly.
 *
 * - We avoid repeatedly asking if a chunk is available by trying to re-use the previous computation if it involves the
 *   same chunk, reducing a lot of map operations elsewhere.
 *
 * - Ticks are stored in a HashMap with their execution state, meaning that redstone gates and other blocks which check
 *   to see if something is scheduled/executing will not have to scan a potentially very large array every time.
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

    public void enqueueTick(ScheduledTick<T> tick) {
        TickEntry<T> entry = this.scheduledTicks.computeIfAbsent(tick, TickEntry::new);

        if (!entry.scheduled) {
            TickEntryQueue<T> timeIdx = this.scheduledTicksOrdered.computeIfAbsent(getBucketKey(tick.time, tick.priority), key -> new TickEntryQueue<>());
            timeIdx.push(entry);

            entry.scheduled = true;
        }
    }

    public void selectTicks(ServerChunkManager chunkManager, long time) {
        long headKey = ((time + 1L) << 4L) - 1L;
        int limit = 65565;

        boolean canTick = true;
        long prevChunk = Long.MIN_VALUE;

        Iterator<TickEntryQueue<T>> it = this.scheduledTicksOrdered.headMap(headKey).values().iterator();

        while (limit > 0 && it.hasNext()) {
            TickEntryQueue<T> list = it.next();

            int w = 0;

            for (int i = 0; i < list.size(); i++) {
                TickEntry<T> tick = list.getTickAtIndex(i);

                if (!tick.scheduled) {
                    continue;
                }

                if (limit > 0) {
                    long chunk = ChunkPos.toLong(tick.pos.getX() >> 4, tick.pos.getZ() >> 4);

                    if (prevChunk != chunk) {
                        prevChunk = chunk;
                        canTick = chunkManager.shouldTickBlock(tick.pos);
                    }

                    if (canTick) {
                        tick.scheduled = false;
                        tick.executing = true;

                        this.executingTicks.add(tick);

                        limit--;

                        continue;
                    }
                }

                list.setTickAtIndex(w++, tick);
            }

            list.resize(w);

            if (list.isEmpty()) {
                it.remove();
            }
        }
    }

    public void executeTicks(Consumer<ScheduledTick<T>> consumer) {
        for (TickEntry<T> entry : this.executingTicks) {
            try {
                entry.executing = false;
                entry.consumed = true;

                consumer.accept(entry);

                if (!entry.scheduled) {
                    this.scheduledTicks.remove(entry);
                }
            } catch (Throwable e) {
                CrashReport crash = CrashReport.create(e, "Exception while ticking");
                CrashReportSection section = crash.addElement("Block being ticked");
                CrashReportSection.addBlockInfo(section, entry.pos, null);

                throw new CrashException(crash);
            }
        }

        this.executingTicks.clear();
    }


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

    private List<ScheduledTick<T>> removeTicks(BlockBox box, Predicate<TickEntry<?>> predicate) {
        List<ScheduledTick<T>> ret = new ArrayList<>();

        for (Iterator<TickEntryQueue<T>> timeIdxIt = this.scheduledTicksOrdered.values().iterator(); timeIdxIt.hasNext(); ) {
            TickEntryQueue<T> list = timeIdxIt.next();

            int w = 0;

            for (int i = 0; i < list.size(); i++) {
                TickEntry<T> tick = list.getTickAtIndex(i);

                if (!box.contains(tick.pos) || !predicate.test(tick)) {
                    list.setTickAtIndex(w++, tick);

                    continue;
                }

                tick.scheduled = false;

                ret.add(tick);
            }

            list.resize(w);

            if (list.isEmpty()) {
                timeIdxIt.remove();
            }
        }

        return ret;
    }

    private void addScheduledTick(ScheduledTick<T> tick) {
        this.enqueueTick(tick);
    }

    // Computes a timestamped key including the tick's priority
    // Keys can be sorted in ascending order to find what should be executed first
    // 60 time bits, 4 priority bits
    private static long getBucketKey(long time, TickPriority priority) {
        return (time << 4L) | (priority.ordinal() & 15);
    }
}