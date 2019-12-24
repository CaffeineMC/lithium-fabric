package me.jellysquid.mods.lithium.common.world.scheduler;

import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ScheduledTick;
import net.minecraft.world.TickPriority;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

class ScheduledTickMap<T> {
    private final Long2ObjectSortedMap<UpdateTimeIndex<T>> activeTicksByTime = new Long2ObjectAVLTreeMap<>();

    private final Map<ScheduledTick<T>, TickEntry<T>> scheduled = new HashMap<>();

    public void enqueueTick(ScheduledTick<T> tick) {
        TickEntry<T> entry = this.scheduled.computeIfAbsent(tick, TickEntry::new);

        if (!entry.scheduled) {
            UpdateTimeIndex<T> timeIdx = this.activeTicksByTime.computeIfAbsent(getTimeKey(tick.time, tick.priority), UpdateTimeIndex::new);

            UpdateList<T> chunkIdx = timeIdx.computeIfAbsent(getChunkKey(tick.pos), UpdateList::new);
            chunkIdx.add(entry);

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

    public void iterateTicks(BlockBox box, Consumer<TickEntry<T>> consumer, boolean remove) {
        for (ObjectIterator<UpdateTimeIndex<T>> timeIdxIt = this.activeTicksByTime.values().iterator(); timeIdxIt.hasNext(); ) {
            UpdateTimeIndex<T> timeIdx = timeIdxIt.next();

            for (ObjectIterator<UpdateList<T>> chunkIdxIt = timeIdx.values().iterator(); chunkIdxIt.hasNext(); ) {
                UpdateList<T> chunkIdx = chunkIdxIt.next();

                int x = chunkIdx.getX();
                int z = chunkIdx.getZ();

                if (!box.intersectsXZ(x, z, x + 16, z + 16)) {
                    continue;
                }

                for (Iterator<TickEntry<T>> entryIt = chunkIdx.iterator(); entryIt.hasNext(); ) {
                    TickEntry<T> entry = entryIt.next();

                    if (!box.contains(entry.tick.pos)) {
                        continue;
                    }

                    consumer.accept(entry);

                    if (remove) {
                        entryIt.remove();

                        this.scheduled.remove(entry.tick);
                    }
                }

                if (remove && chunkIdx.isEmpty()) {
                    chunkIdxIt.remove();
                }
            }

            if (remove && timeIdx.isEmpty()) {
                timeIdxIt.remove();
            }
        }
    }

    private final ArrayList<UpdateList<T>> selectedTickLists = new ArrayList<>();

    public void selectTicksForExecution(ServerChunkManager chunks, long time) {
        final BlockPos.Mutable pos = new BlockPos.Mutable();

        for (ObjectIterator<Long2ObjectMap.Entry<UpdateTimeIndex<T>>> timeIdxIt = this.activeTicksByTime.headMap(time << 4L).long2ObjectEntrySet().iterator(); timeIdxIt.hasNext(); ) {
            Long2ObjectMap.Entry<UpdateTimeIndex<T>> entry = timeIdxIt.next();

            UpdateTimeIndex<T> timeIdx = entry.getValue();

            for (ObjectIterator<UpdateList<T>> chunkIdxIt = timeIdx.values().iterator(); chunkIdxIt.hasNext(); ) {
                UpdateList<T> chunkIdx = chunkIdxIt.next();

                // Hack to determine if the chunk is loaded
                if (chunks.shouldTickBlock(pos.set(chunkIdx.getX() + 8, 0, chunkIdx.getZ() + 8))) {
                    for (TickEntry<T> tickEntry : chunkIdx) {
                        tickEntry.scheduled = false;
                        tickEntry.executing = true;
                    }

                    this.selectedTickLists.add(chunkIdx);

                    chunkIdxIt.remove();
                }
            }

            if (timeIdx.isEmpty()) {
                timeIdxIt.remove();
            }
        }
    }

    public void performTicks(Consumer<ScheduledTick<T>> consumer) {
        // Execute all pending ticks
        for (UpdateList<T> list : this.selectedTickLists) {
            this.executeList(list, consumer);
        }

        // Remove all ticks which haven't been re-scheduled
        for (UpdateList<T> list : this.selectedTickLists) {
            for (TickEntry<T> entry : list) {
                if (!entry.scheduled) {
                    this.scheduled.remove(entry.tick);
                }
            }
        }

        this.selectedTickLists.clear();
    }

    private void executeList(UpdateList<T> list, Consumer<ScheduledTick<T>> consumer) {
        for (TickEntry<T> entry : list) {
            try {
                entry.executing = false;
                entry.consumed = true;

                consumer.accept(entry.tick);
            } catch (Throwable e) {
                CrashReport crash = CrashReport.create(e, "Exception while ticking");
                CrashReportSection section = crash.addElement("Block being ticked");
                CrashReportSection.addBlockInfo(section, entry.tick.pos, null);
                throw new CrashException(crash);
            }
        }
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

    private static class UpdateTimeIndex<T> extends Long2ObjectOpenHashMap<UpdateList<T>> {
        private UpdateTimeIndex(long key) { }
    }

    private static class UpdateList<T> extends ArrayList<TickEntry<T>> {
        public final long key;

        private UpdateList(long key) {
            this.key = key;
        }

        public int getX() {
            return ChunkPos.getPackedX(this.key) << 4;
        }

        public int getZ() {
            return ChunkPos.getPackedZ(this.key) << 4;
        }
    }

    // Wrapper to extend ScheduledTick, adds some overhead.
    public static class TickEntry<T> {
        public final ScheduledTick<T> tick;

        public boolean scheduled = false;
        public boolean executing = false;
        public boolean consumed = false;

        public TickEntry(ScheduledTick<T> tick) {
            this.tick = tick;
        }
    }

    private static long getTimeKey(long time, TickPriority priority) {
        return (time << 4L) | (priority.ordinal() & 15);
    }

    private static long getChunkKey(BlockPos pos) {
        return ChunkPos.toLong(pos.getX() >> 4, pos.getZ() >> 4);
    }
}
