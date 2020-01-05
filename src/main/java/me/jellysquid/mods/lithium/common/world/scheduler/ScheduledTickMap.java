package me.jellysquid.mods.lithium.common.world.scheduler;

import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMap;
import net.minecraft.server.world.ServerChunkManager;
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
import java.util.function.Predicate;

class ScheduledTickMap<T> {
    public static final int INITIAL_UPDATE_LIST_CAPACITY = 10;

    private final Long2ObjectSortedMap<UpdateList<T>> activeTicksByTime = new Long2ObjectAVLTreeMap<>();

    private final Map<ScheduledTick<T>, TickEntry<T>> scheduled = new HashMap<>();

    private final ArrayList<TickEntry<T>> selectedTicks = new ArrayList<>();

    public void enqueueTick(ScheduledTick<T> tick) {
        TickEntry<T> entry = this.scheduled.computeIfAbsent(tick, TickEntry::new);

        if (!entry.scheduled) {
            UpdateList<T> timeIdx = this.activeTicksByTime.computeIfAbsent(getTimeKey(tick.time, tick.priority), UpdateList::new);
            timeIdx.add(entry);

            entry.scheduled = true;
        }
    }

    public boolean isExecuting(BlockPos pos, T obj) {
        TickEntry<T> entry = this.scheduled.get(new ScheduledTick<>(pos, obj));

        if (entry == null) {
            return false;
        }

        return entry.executing;
    }

    public boolean isScheduled(BlockPos pos, T obj) {
        TickEntry<T> entry = this.scheduled.get(new ScheduledTick<>(pos, obj));

        if (entry == null) {
            return false;
        }

        return entry.scheduled;
    }

    public List<ScheduledTick<T>> copyTicks(BlockBox box, Predicate<TickEntry<?>> predicate, boolean remove) {
        return remove ? this.removeTicks(box, predicate) : this.collectTicks(box, predicate);
    }

    public List<ScheduledTick<T>> collectTicks(BlockBox box, Predicate<TickEntry<?>> predicate) {
        List<ScheduledTick<T>> ret = new ArrayList<>();

        for (UpdateList<T> ticks : this.activeTicksByTime.values()) {
            for (int i = 0; i < ticks.size(); i++) {
                TickEntry<T> tick = ticks.get(i);

                if (box.contains(tick.pos) && predicate.test(tick)) {
                    ret.add(tick);
                }
            }
        }

        return ret;
    }

    public List<ScheduledTick<T>> removeTicks(BlockBox box, Predicate<TickEntry<?>> predicate) {
        List<ScheduledTick<T>> ret = new ArrayList<>();

        for (Iterator<UpdateList<T>> timeIdxIt = this.activeTicksByTime.values().iterator(); timeIdxIt.hasNext(); ) {
            UpdateList<T> ticks = timeIdxIt.next();

            int j = 0;

            for (int i = 0; i < ticks.size(); i++) {
                TickEntry<T> tick = ticks.get(i);

                if (!box.contains(tick.pos) || !predicate.test(tick)) {
                    ticks.set(j++, tick);

                    continue;
                }

                ret.add(tick);

                this.scheduled.remove(tick);
            }

            ticks.setSize(j);

            if (j == 0) {
                timeIdxIt.remove();
            }
        }

        return ret;
    }

    public void selectTicksForExecution(ServerChunkManager chunks, long time) {
        long headKey = ((time + 1L) << 4L) - 1L;
        int limit = 65565;

        boolean canTick = true;
        long prevChunk = Long.MIN_VALUE;

        Iterator<UpdateList<T>> listsIt = this.activeTicksByTime.headMap(headKey).values().iterator();

        while (limit > 0 && listsIt.hasNext()) {
            UpdateList<T> ticks = listsIt.next();

            int j = 0;

            for (int i = 0; i < ticks.size(); i++) {
                TickEntry<T> tick = ticks.get(i);

                if (limit > 0) {
                    long chunk = ChunkPos.toLong(tick.pos.getX() >> 4, tick.pos.getZ() >> 4);

                    if (prevChunk != chunk) {
                        prevChunk = chunk;
                        canTick = chunks.shouldTickBlock(tick.pos);
                    }

                    if (canTick) {
                        tick.scheduled = false;
                        tick.executing = true;

                        this.selectedTicks.add(tick);

                        limit--;

                        continue;
                    }
                }

                ticks.set(j++, tick);
            }

            ticks.setSize(j);

            if (j == 0) {
                listsIt.remove();
            }
        }
    }

    public void performTicks(Consumer<ScheduledTick<T>> consumer) {
        for (TickEntry<T> entry : this.selectedTicks) {
            try {
                entry.executing = false;
                entry.consumed = true;

                consumer.accept(entry);

                if (!entry.scheduled) {
                    this.scheduled.remove(entry);
                }
            } catch (Throwable e) {
                CrashReport crash = CrashReport.create(e, "Exception while ticking");
                CrashReportSection section = crash.addElement("Block being ticked");
                CrashReportSection.addBlockInfo(section, entry.pos, null);

                throw new CrashException(crash);
            }
        }

        this.selectedTicks.clear();
    }

    public int getScheduledCount() {
        int count = 0;

        for (TickEntry<T> entry : this.scheduled.values()) {
            if (entry.scheduled) {
                count += 1;
            }
        }

        return count;
    }

    public static class TickEntry<T> extends ScheduledTick<T> {
        public boolean scheduled = false;
        public boolean executing = false;
        public boolean consumed = false;

        public TickEntry(ScheduledTick<T> tick) {
            super(tick.pos, tick.getObject(), tick.time, tick.priority);
        }
    }

    private static long getTimeKey(long time, TickPriority priority) {
        return (time << 4L) | (priority.ordinal() & 15);
    }
}
