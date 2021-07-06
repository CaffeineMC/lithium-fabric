package me.jellysquid.mods.lithium.mixin.ai.task.replace_streams;

import me.jellysquid.mods.lithium.common.ai.WeightedListIterable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.CompositeTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.WeightedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(CompositeTask.class)
public class CompositeTaskMixin<E extends LivingEntity> {
    @Shadow
    @Final
    private WeightedList<Task<? super E>> tasks;

    @Shadow
    @Final
    private Set<MemoryModuleType<?>> memoriesToForgetWhenStopped;

    /**
     * @reason Replace stream code with traditional iteration
     * @author JellySquid
     */
    @Overwrite
    public boolean shouldKeepRunning(ServerWorld world, E entity, long time) {
        for (Task<? super E> task : WeightedListIterable.cast(this.tasks)) {
            if (task.getStatus() == Task.Status.RUNNING) {
                if (task.shouldKeepRunning(world, entity, time)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @reason Replace stream code with traditional iteration
     * @author JellySquid
     */
    @Overwrite
    public void keepRunning(ServerWorld world, E entity, long time) {
        for (Task<? super E> task : WeightedListIterable.cast(this.tasks)) {
            if (task.getStatus() == Task.Status.RUNNING) {
                task.tick(world, entity, time);
            }
        }
    }

    /**
     * @reason Replace stream code with traditional iteration
     * @author JellySquid
     */
    @Overwrite
    public void finishRunning(ServerWorld world, E entity, long time) {
        for (Task<? super E> task : WeightedListIterable.cast(this.tasks)) {
            if (task.getStatus() == Task.Status.RUNNING) {
                task.stop(world, entity, time);
            }
        }

        Brain<?> brain = entity.getBrain();

        for (MemoryModuleType<?> module : this.memoriesToForgetWhenStopped) {
            brain.forget(module);
        }
    }
}
