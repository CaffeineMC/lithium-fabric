package me.jellysquid.mods.lithium.common.world.scheduler;

import com.google.common.collect.Lists;
import me.jellysquid.mods.lithium.common.world.scheduler.ScheduledTickMap.Status;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
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
import java.util.stream.Stream;

public class LithiumServerTickScheduler<T> extends ServerTickScheduler<T> {
    private final Predicate<T> invalidObjPredicate;
    private final Function<T, Identifier> idToName;
    private final Function<Identifier, T> nameToId;
    private final ServerWorld world;
    private final Consumer<ScheduledTick<T>> tickConsumer;
    private final ScheduledTickMap<T> tickMap = new ScheduledTickMap<>();

    public LithiumServerTickScheduler(ServerWorld world, Predicate<T> invalidPredicate, Function<T, Identifier> idToName, Function<Identifier, T> nameToId, Consumer<ScheduledTick<T>> consumer) {
        super(world, invalidPredicate, idToName, nameToId, consumer);

        this.invalidObjPredicate = invalidPredicate;
        this.idToName = idToName;
        this.nameToId = nameToId;
        this.world = world;
        this.tickConsumer = consumer;
    }

    @Override
    public void tick() {
        this.world.getProfiler().push("cleaning");

        this.tickMap.cleanup(this.world.getChunkManager(), this.world.getTime() + 1);

        this.world.getProfiler().swap("executing");

        this.tickMap.performTicks(this.tickConsumer);

        this.world.getProfiler().pop();
    }

    @Override
    public boolean isTicking(BlockPos pos, T obj) {
        return this.tickMap.getScheduledTickStatus(pos, obj, true);
    }

    @Override
    public void scheduleAll(Stream<ScheduledTick<T>> stream) {
        stream.forEach(this::addScheduledTick);
    }

    @Override
    public List<ScheduledTick<T>> getScheduledTicksInChunk(ChunkPos chunkPos, boolean consumed, boolean remove) {
        List<ScheduledTick<T>> ret = new ArrayList<>();

        Iterator<ScheduledTickMap.UpdateList<T>> listIt = this.tickMap.getTicksForChunk(chunkPos.toLong());

        while (listIt.hasNext()) {
            ScheduledTickMap.UpdateList<T> next = listIt.next();

            for (Iterator<ScheduledTickMap.Mut<T>> mutIt = next.iterator(); mutIt.hasNext(); ) {
                ScheduledTickMap.Mut<T> mut = mutIt.next();

                if (mut.status == Status.CONSUMED && !consumed) {
                    continue;
                }

                ScheduledTick<T> tick = mut.tick;
                ret.add(tick);

                if (remove) {
                    mutIt.remove();

                    this.tickMap.removeTick(tick);
                }
            }
        }

        return ret;
    }

    @Override
    public List<ScheduledTick<T>> getScheduledTicks(BlockBox box, boolean remove, boolean includeConsumed) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void copyScheduledTicks(BlockBox box, BlockPos pos) {
        List<ScheduledTick<T>> ret = null;

        for (ScheduledTickMap.Mut<T> mut : this.tickMap.getAllTicks()) {
            ScheduledTick<T> tick = mut.tick;

            if (tick.pos.getX() >= box.minX && tick.pos.getX() < box.maxX && tick.pos.getZ() >= box.minZ && tick.pos.getZ() < box.maxZ) {
                if (ret == null) {
                    ret = Lists.newArrayList();
                }

                ret.add(tick);
            }
        }

        if (ret == null) {
            return;
        }

        for (ScheduledTick<T> tick : ret) {
            if (box.contains(tick.pos)) {
                this.addScheduledTick(new ScheduledTick<>(tick.pos.add(pos), tick.getObject(), tick.time, tick.priority));
            }
        }
    }

    @Override
    public ListTag toTag(ChunkPos chunkPos) {
        List<ScheduledTick<T>> ticks = this.getScheduledTicksInChunk(chunkPos, false, true);
        return serializeScheduledTicks(this.idToName, ticks, this.world.getTime());
    }

    public static <T> ListTag serializeScheduledTicks(Function<T, Identifier> function_1, Iterable<ScheduledTick<T>> ticks, long offset) {
        ListTag listTag_1 = new ListTag();

        for (ScheduledTick<T> tick : ticks) {
            CompoundTag compoundTag_1 = new CompoundTag();
            compoundTag_1.putString("i", function_1.apply(tick.getObject()).toString());
            compoundTag_1.putInt("x", tick.pos.getX());
            compoundTag_1.putInt("y", tick.pos.getY());
            compoundTag_1.putInt("z", tick.pos.getZ());
            compoundTag_1.putInt("t", (int) (tick.time - offset));
            compoundTag_1.putInt("p", tick.priority.getIndex());
            listTag_1.add(compoundTag_1);
        }

        return listTag_1;
    }

    @Override
    public boolean isScheduled(BlockPos pos, T obj) {
        return this.tickMap.getScheduledTickStatus(pos, obj, false);
    }

    @Override
    public void schedule(BlockPos pos, T obj, int delay, TickPriority priority) {
        if (!this.invalidObjPredicate.test(obj)) {
            this.addScheduledTick(new ScheduledTick<>(pos, obj, (long) delay + this.world.getTime(), priority));
        }
    }

    private void addScheduledTick(ScheduledTick<T> tick) {
        this.tickMap.addScheduledTick(tick);
    }

    @Override
    public int method_20825() {
        return this.tickMap.getScheduledCount();
    }
}