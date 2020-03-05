package me.jellysquid.mods.lithium.mixin.ai.fast_brain;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

/**
 * A significant amount of overhead in entity ticking comes from mob brains iterating over their list of tasks. This
 * patch replaces the stream-based code with more traditional iteration and then flattens out the nested iteration
 * into simple arrays that can be quickly scanned over, providing a massive speedup and reduction to memory
 * allocations.
 */
@Mixin(Brain.class)
public abstract class MixinBrain<E extends LivingEntity> {
    @Shadow
    @Final
    private Map<MemoryModuleType<?>, Optional<? extends Memory<?>>> memories;

    @Shadow
    @Final
    private Map<SensorType<? extends Sensor<? super E>>, Sensor<? super E>> sensors;

    @Shadow
    @Final
    private Map<Integer, Map<Activity, Set<Task<? super E>>>> tasks;

    @Shadow
    @Final
    private Set<Activity> possibleActivities;

    @Shadow
    public abstract <U> void forget(MemoryModuleType<U> memoryModuleType_1);

    private final List<Pair<Activity, List<Task<? super E>>>> allTasks = new ArrayList<>();

    @Inject(method = "setTaskList(Lnet/minecraft/entity/ai/brain/Activity;Lcom/google/common/collect/ImmutableList;Ljava/util/Set;Ljava/util/Set;)V", at = @At("RETURN"))
    private void onTaskListUpdated(Activity activity, ImmutableList<? extends Pair<Integer, ? extends Task<? super E>>> immutableList_1, Set<Pair<MemoryModuleType<?>, MemoryModuleState>> set_1, Set<MemoryModuleType<?>> set_2, CallbackInfo ci) {
        this.allTasks.clear();

        // Re-build the sorted list of tasks, flattening it into a simple array
        // This will only happen a few times during during entity initialization
        for (Map<Activity, Set<Task<? super E>>> map : this.tasks.values()) {
            for (Map.Entry<Activity, Set<Task<? super E>>> entry : map.entrySet()) {
                this.allTasks.add(Pair.of(entry.getKey(), new ArrayList<>(entry.getValue())));
            }
        }
    }

    /**
     * @reason Replace stream-based code with traditional iteration, use flattened collection type
     * @author JellySquid
     */
    @Overwrite
    public void tick(ServerWorld world, E entity) {
        this.expireOutdatedMemories();

        this.updateSensors(world, entity);

        this.startTasks(world, entity);
        this.updateTasks(world, entity);
    }

    /**
     * @reason Replace stream-based code with traditional iteration, use flattened collection type
     * @author JellySquid
     */
    @Overwrite
    private void startTasks(ServerWorld world, E entity) {
        long time = world.getTime();

        for (Pair<Activity, List<Task<? super E>>> pair : this.allTasks) {
            if (!this.possibleActivities.contains(pair.getFirst())) {
                continue;
            }

            for (Task<? super E> task : pair.getSecond()) {
                if (task.getStatus() == Task.Status.STOPPED) {
                    task.tryStarting(world, entity, time);
                }
            }
        }
    }

    /**
     * @reason Replace stream-based code with traditional iteration, use flattened collection type
     * @author JellySquid
     */
    @Overwrite
    private void updateTasks(ServerWorld world, E entity) {
        long time = world.getTime();

        for (Pair<Activity, List<Task<? super E>>> pair : this.allTasks) {
            for (Task<? super E> task : pair.getSecond()) {
                if (task.getStatus() == Task.Status.RUNNING) {
                    task.tick(world, entity, time);
                }
            }
        }
    }


    private void expireOutdatedMemories() {
        for (Map.Entry<MemoryModuleType<?>, Optional<? extends Memory<?>>> entry : this.memories.entrySet()) {
            Memory<?> memory = entry.getValue().orElse(null);

            if (memory != null) {
                memory.method_24913();

                if (memory.isExpired()) {
                    this.forget(entry.getKey());
                }
            }
        }
    }

    private void updateSensors(ServerWorld world, E entity) {
        for (Sensor<? super E> sensor : this.sensors.values()) {
            sensor.canSense(world, entity);
        }
    }
}
