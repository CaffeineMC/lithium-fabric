package me.jellysquid.mods.lithium.common.world.scheduler;

import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.TaskPriority;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ScheduledTick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

class ScheduledTickMap<T> {
    private final Long2ObjectSortedMap<UpdateTimeIndex<T>> activeTicksByTime = new Long2ObjectAVLTreeMap<>();
    private final Map<ScheduledTick<T>, Mut<T>> scheduled = new HashMap<>();

    void addScheduledTick(ScheduledTick<T> tick) {
        Mut<T> mut = this.scheduled.computeIfAbsent(tick, Mut::new);

        if (mut.status == Status.SCHEDULED) {
            return;
        }

        mut.status = Status.SCHEDULED;

        UpdateList<T> idx = this.activeTicksByTime.computeIfAbsent(getTimeKey(tick.time, tick.priority), UpdateTimeIndex::new)
                .computeIfAbsent(getChunkKey(tick.pos), UpdateList::new);
        idx.add(mut);
    }

    private static long getChunkKey(BlockPos pos) {
        return ChunkPos.toLong(pos.getX() >> 4, pos.getZ() >> 4);
    }

    boolean getScheduledTickStatus(BlockPos pos, T obj, boolean executing) {
        Mut<T> index = this.scheduled.get(new ScheduledTick<>(pos, obj));

        return index != null && index.status == (executing ? Status.EXECUTING : Status.SCHEDULED);
    }

    private static long getTimeKey(long time, TaskPriority priority) {
        return (time << 4L) | (priority.ordinal() & 15);
    }

    Iterator<UpdateList<T>> getTicksForChunk(long chunk) {
        return this.activeTicksByTime
                .values()
                .stream()
                .filter(table -> table.key == chunk)
                .flatMap(table -> table.values().stream())
                .iterator();
    }

    Iterable<Mut<T>> getAllTicks() {
        return this.scheduled.values();
    }

    private final ArrayList<UpdateList<T>> updating = new ArrayList<>();

    void cleanup(ServerChunkManager chunks, long time) {
        final BlockPos.Mutable pos = new BlockPos.Mutable();

        for (ObjectIterator<UpdateTimeIndex<T>> timeIdxIt = this.activeTicksByTime.headMap(time << 4L).values().iterator(); timeIdxIt.hasNext(); ) {
            UpdateTimeIndex<T> table = timeIdxIt.next();

            for (Iterator<UpdateList<T>> chunkIdxIt = table.values().iterator(); chunkIdxIt.hasNext(); ) {
                UpdateList<T> list = chunkIdxIt.next();

                int chunkX = ChunkPos.getPackedX(list.key);
                int chunkZ = ChunkPos.getPackedZ(list.key);

                if (list.executed) {
                    chunkIdxIt.remove();

                    continue;
                }

                // Hack to determine if the chunk is loaded
                if (chunks.shouldTickBlock(pos.set(chunkX << 4, 0, chunkZ << 4))) {
                    for (Mut<T> mut : list) {
                        mut.status = Status.EXECUTING;
                    }

                    this.updating.add(list);

                    list.executed = true;
                }
            }

            if (table.isEmpty()) {
                timeIdxIt.remove();
            }
        }
    }

    void performTicks(Consumer<ScheduledTick<T>> consumer) {
        for (UpdateList<T> list : this.updating) {
            this.execute(list, consumer);
        }

        this.updating.clear();
    }

    private void execute(UpdateList<T> list, Consumer<ScheduledTick<T>> consumer) {
        for (Mut<T> mut : list) {
            try {
                mut.status = Status.CONSUMED;

                consumer.accept(mut.tick);
            } catch (Throwable e) {
                CrashReport crash = CrashReport.create(e, "Exception while ticking");
                CrashReportSection section = crash.addElement("Block being ticked");
                CrashReportSection.addBlockInfo(section, mut.tick.pos, null);
                throw new CrashException(crash);
            }
        }
    }

    int getScheduledCount() {
        int count = 0;

        for (Mut<T> mut : this.scheduled.values()) {
            if (mut.status == Status.SCHEDULED) {
                count += 1;
            }
        }

        return count;
    }

    private static class UpdateTimeIndex<T> extends Long2ObjectOpenHashMap<UpdateList<T>> {
        private final long key;

        private UpdateTimeIndex(long key) {
            this.key = key;
        }
    }

    static class UpdateList<T> extends ArrayList<Mut<T>> {
        private final long key;
        private boolean executed = false;

        private UpdateList(long key) {
            this.key = key;
        }
    }

    enum Status {
        SCHEDULED,
        EXECUTING,
        CONSUMED
    }

    static class Mut<T> {
        final ScheduledTick<T> tick;

        Status status = null;

        private Mut(ScheduledTick<T> tick) {
            this.tick = tick;
        }
    }

    void removeTick(ScheduledTick<T> tick) {
        this.scheduled.remove(tick);
    }
}
