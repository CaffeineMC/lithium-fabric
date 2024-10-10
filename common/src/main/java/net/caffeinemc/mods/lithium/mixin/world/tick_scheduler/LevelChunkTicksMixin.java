package net.caffeinemc.mods.lithium.mixin.world.tick_scheduler;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2ReferenceAVLTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.caffeinemc.mods.lithium.common.world.scheduler.OrderedTickQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.SavedTick;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(LevelChunkTicks.class)
public class LevelChunkTicksMixin<T> {
    private static volatile Reference2IntOpenHashMap<Object> TYPE_2_INDEX;

    static {
        TYPE_2_INDEX = new Reference2IntOpenHashMap<>();
        TYPE_2_INDEX.defaultReturnValue(-1);
    }

    private final Long2ReferenceAVLTreeMap<OrderedTickQueue<T>> tickQueuesByTimeAndPriority = new Long2ReferenceAVLTreeMap<>();
    private OrderedTickQueue<T> nextTickQueue;
    private final IntOpenHashSet allTicks = new IntOpenHashSet();

    @Shadow
    private @Nullable BiConsumer<LevelChunkTicks<T>, ScheduledTick<T>> onTickAdded;

    @Mutable
    @Shadow
    @Final
    private Set<ScheduledTick<?>> ticksPerPosition;

    @Shadow
    private @Nullable List<SavedTick<T>> pendingTicks;

    @Mutable
    @Shadow
    @Final
    private Queue<ScheduledTick<T>> tickQueue;

    @Inject(
            method = {"<init>()V", "<init>(Ljava/util/List;)V"},
            at = @At("RETURN")
    )
    private void reinit(CallbackInfo ci) {
        //Remove replaced collections
        if (this.pendingTicks != null) {
            for (SavedTick<?> orderedTick : this.pendingTicks) {
                this.allTicks.add(tickToInt(orderedTick.pos(), orderedTick.type()));
            }
        }
        this.ticksPerPosition = null;
        this.tickQueue = null;
    }

    private static int tickToInt(BlockPos pos, Object type) {
        //Y coordinate is 12 bits (BlockPos.toLong)
        //X and Z coordinate is 4 bits each (This scheduler is for a single chunk)
        //20 bits are in use for pos
        //12 bits remaining for the type, so up to 4096 different tickable blocks/fluids (not block states) -> can upgrade to long if needed
        int typeIndex = TYPE_2_INDEX.getInt(type);
        if (typeIndex == -1) {
            typeIndex = fixMissingType2Index(type);
        }

        int ret = ((pos.getX() & 0xF) << 16) | ((pos.getY() & (0xfff)) << 4) | (pos.getZ() & 0xF);
        ret |= typeIndex << 20;
        return ret;
    }

    //This method must be synchronized, otherwise type->int assignments can be overwritten and therefore change
    //Uses clone and volatile store to ensure only fully initialized maps are used, all threads share the same mapping
    private static synchronized int fixMissingType2Index(Object type) {
        //check again, other thread might have replaced the collection
        int typeIndex = TYPE_2_INDEX.getInt(type);
        if (typeIndex == -1) {
            Reference2IntOpenHashMap<Object> clonedType2Index = TYPE_2_INDEX.clone();
            clonedType2Index.put(type, typeIndex = clonedType2Index.size());
            TYPE_2_INDEX = clonedType2Index;
            if (typeIndex >= 4096) {
                throw new IllegalStateException("Lithium Tick Scheduler assumes at most 4096 different block types that receive scheduled ticks exist! Add mixin.world.tick_scheduler=false to the lithium properties/config to disable the optimization!");
            }
        }
        return typeIndex;
    }

    /**
     * @author 2No2Name
     * @reason use faster collections
     */
    @Overwrite
    public void schedule(ScheduledTick<T> orderedTick) {
        int intTick = tickToInt(orderedTick.pos(), orderedTick.type());
        if (this.allTicks.add(intTick)) {
            this.queueTick(orderedTick);
        }
    }

    // Computes a timestamped key including the tick's priority
    // Keys can be sorted in descending order to find what should be executed first
    // 60 time bits, 4 priority bits
    private static long getBucketKey(long time, TickPriority priority) {
        //using priority.ordinal() as is not negative instead of priority.index
        return (time << 4L) | (priority.ordinal() & 15);
    }

    private void updateNextTickQueue(boolean checkEmpty) {
        if (checkEmpty && this.nextTickQueue != null && this.nextTickQueue.isEmpty()) {
            OrderedTickQueue<T> removed = this.tickQueuesByTimeAndPriority.remove(this.tickQueuesByTimeAndPriority.firstLongKey());
            if (removed != this.nextTickQueue) {
                throw new IllegalStateException("Next tick queue doesn't have the lowest key!");
            }
        }
        if (this.tickQueuesByTimeAndPriority.isEmpty()) {
            this.nextTickQueue = null;
            return;
        }
        long firstKey = this.tickQueuesByTimeAndPriority.firstLongKey();
        this.nextTickQueue = this.tickQueuesByTimeAndPriority.get(firstKey);
    }

    /**
     * @author 2No2Name
     * @reason use faster collections
     */
    @Overwrite
    @Nullable
    public ScheduledTick<T> peek() {
        if (this.nextTickQueue == null) {
            return null;
        }
        return this.nextTickQueue.peek();
    }

    /**
     * @author 2No2Name
     * @reason use faster collections
     */
    @Overwrite
    @Nullable
    public ScheduledTick<T> poll() {
        ScheduledTick<T> orderedTick = this.nextTickQueue.poll();
        if (orderedTick != null) {
            if (this.nextTickQueue.isEmpty()) {
                this.updateNextTickQueue(true);
            }
            this.allTicks.remove(tickToInt(orderedTick.pos(), orderedTick.type()));
            return orderedTick;
        }
        return null;
    }


    private void queueTick(ScheduledTick<T> orderedTick) {
        OrderedTickQueue<T> tickQueue = this.tickQueuesByTimeAndPriority.computeIfAbsent(getBucketKey(orderedTick.triggerTick(), orderedTick.priority()), key -> new OrderedTickQueue<>());
        if (tickQueue.isEmpty()) {
            this.updateNextTickQueue(false);
        }
        tickQueue.offer(orderedTick);

        if (this.onTickAdded != null) {
            //noinspection unchecked
            this.onTickAdded.accept((LevelChunkTicks<T>) (Object) this, orderedTick);
        }
    }

    /**
     * @author 2No2Name
     * @reason use faster collections
     */
    @Overwrite
    public boolean hasScheduledTick(BlockPos pos, T type) {
        return this.allTicks.contains(tickToInt(pos, type));
    }

    /**
     * @author 2No2Name
     * @reason use faster collections
     */
    @Overwrite
    public void removeIf(Predicate<ScheduledTick<T>> predicate) {
        for (ObjectIterator<OrderedTickQueue<T>> tickQueueIterator = this.tickQueuesByTimeAndPriority.values().iterator(); tickQueueIterator.hasNext(); ) {
            OrderedTickQueue<T> nextTickQueue = tickQueueIterator.next();
            nextTickQueue.sort();
            boolean removed = false;
            for (int i = 0; i < nextTickQueue.size(); i++) {
                ScheduledTick<T> nextTick = nextTickQueue.getTickAtIndex(i);
                if (predicate.test(nextTick)) {
                    nextTickQueue.setTickAtIndex(i, null);
                    this.allTicks.remove(tickToInt(nextTick.pos(), nextTick.type()));
                    removed = true;
                }
            }
            if (removed) {
                nextTickQueue.removeNullsAndConsumed();
            }
            if (nextTickQueue.isEmpty()) {
                tickQueueIterator.remove();
            }
        }
        this.updateNextTickQueue(false);
    }

    /**
     * @author 2No2Name
     * @reason use faster collections
     */
    @Overwrite
    public Stream<ScheduledTick<T>> getAll() {
        return this.tickQueuesByTimeAndPriority.values().stream().flatMap(Collection::stream);
    }


    /**
     * @author 2No2Name
     * @reason not use unused field
     */
    @Overwrite
    public int count() {
        return this.allTicks.size();
    }

    /**
     * @author 2No2Name
     * @reason not use unused field
     */
    @Overwrite
    public ListTag save(long l, Function<T, String> function) {
        ListTag nbtList = new ListTag();
        if (this.pendingTicks != null) {
            for (SavedTick<T> tick : this.pendingTicks) {
                nbtList.add(tick.save(function));
            }
        }
        for (OrderedTickQueue<T> nextTickQueue : this.tickQueuesByTimeAndPriority.values()) {
            for (ScheduledTick<T> orderedTick : nextTickQueue) {
                nbtList.add(SavedTick.saveTick(orderedTick, function, l));
            }
        }
        return nbtList;
    }


    /**
     * @author 2No2Name
     * @reason use our datastructures
     */
    @Overwrite
    public void unpack(long time) {
        if (this.pendingTicks != null) {
            int i = -this.pendingTicks.size();
            for (SavedTick<T> tick : this.pendingTicks) {
                this.queueTick(tick.unpack(time, i++));
            }
        }
        this.pendingTicks = null;
    }
}
