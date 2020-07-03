package me.jellysquid.mods.lithium.mixin.world.chunk_task_system;

import me.jellysquid.mods.lithium.common.util.thread.ArrayPrioritizedTaskQueue;
import net.minecraft.server.world.ChunkTaskPrioritySystem;
import net.minecraft.util.thread.MessageListener;
import net.minecraft.util.thread.TaskExecutor;
import net.minecraft.util.thread.TaskQueue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * This replaces the queue used by the chunk job executor to a much quicker variant.
 */
@Mixin(ChunkTaskPrioritySystem.class)
public class ChunkTaskPrioritySystemMixin {
    @Mutable
    @Shadow
    @Final
    private TaskExecutor<TaskQueue.PrioritizedTask> sorter;

    /**
     * Re-initialize the task executor with our optimized task queue type. This is a safe operation that happens only
     * once at world load. No tasks will be enqueued until after the constructor is ran, so we do not need to worry
     * about copying them.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(List<MessageListener<?>> listeners, Executor executor, int maxQueues, CallbackInfo ci) {
        this.sorter = new TaskExecutor<>(new ArrayPrioritizedTaskQueue(4), executor, "sorter");
    }
}
