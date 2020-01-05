package me.jellysquid.mods.lithium.common.world.scheduler;

import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ScheduledTick;
import net.minecraft.world.TickPriority;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class LithiumServerTickScheduler<T> extends ServerTickScheduler<T> {
    private static final Predicate<ScheduledTickMap.TickEntry<?>> PREDICATE_ANY_TICK = entry -> true;
    private static final Predicate<ScheduledTickMap.TickEntry<?>> PREDICATE_ACTIVE_TICKS = entry -> !entry.consumed;

    private final Predicate<T> invalidObjPredicate;
    private final ServerWorld world;
    private final Consumer<ScheduledTick<T>> tickConsumer;
    private final ScheduledTickMap<T> tickMap = new ScheduledTickMap<>();

    public LithiumServerTickScheduler(ServerWorld world, Predicate<T> invalidPredicate, Function<T, Identifier> idToName, Function<Identifier, T> nameToId, Consumer<ScheduledTick<T>> consumer) {
        super(world, invalidPredicate, idToName, nameToId, consumer);

        this.invalidObjPredicate = invalidPredicate;
        this.world = world;
        this.tickConsumer = consumer;
    }

    @Override
    public void tick() {
        this.world.getProfiler().push("cleaning");

        this.tickMap.selectTicksForExecution(this.world.getChunkManager(), this.world.getTime());

        this.world.getProfiler().swap("executing");

        this.tickMap.performTicks(this.tickConsumer);

        this.world.getProfiler().pop();
    }

    @Override
    public boolean isTicking(BlockPos pos, T obj) {
        return this.tickMap.isExecuting(pos, obj);
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
        return this.tickMap.copyTicks(box, getStaleTicks ? PREDICATE_ANY_TICK : PREDICATE_ACTIVE_TICKS, remove);
    }

    @Override
    public void copyScheduledTicks(BlockBox box, BlockPos pos) {
        List<ScheduledTick<T>> list = this.getScheduledTicks(box, false, false);

        for (ScheduledTick<T> tick : list) {
            this.addScheduledTick(new ScheduledTick<>(tick.pos.add(pos), tick.getObject(), tick.time, tick.priority));
        }
    }

    @Override
    public boolean isScheduled(BlockPos pos, T obj) {
        return this.tickMap.isScheduled(pos, obj);
    }

    @Override
    public void schedule(BlockPos pos, T obj, int delay, TickPriority priority) {
        if (!this.invalidObjPredicate.test(obj)) {
            this.addScheduledTick(new ScheduledTick<>(pos, obj, (long) delay + this.world.getTime(), priority));
        }
    }

    private void addScheduledTick(ScheduledTick<T> tick) {
        this.tickMap.enqueueTick(tick);
    }

    @Override
    public int method_20825() {
        return this.tickMap.getScheduledCount();
    }
}