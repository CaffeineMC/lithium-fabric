package me.jellysquid.mods.lithium.mixin.ai.brain;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.jellysquid.mods.lithium.common.util.collections.MaskedList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.annotation.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(Brain.class)
public class BrainMixin<E extends LivingEntity> {

    @Shadow
    @Final
    private Map<Integer, Map<Activity, Set<Task<? super E>>>> tasks;

    private MaskedList<Task<? super E>> flatTasks;

    @Inject(
            method = "setTaskList(Lnet/minecraft/entity/ai/brain/Activity;Lcom/google/common/collect/ImmutableList;Ljava/util/Set;Ljava/util/Set;)V",
            at = @At("RETURN")
    )
    private void updateTaskList(Activity activity, ImmutableList<? extends Pair<Integer, ? extends Task<? super E>>> indexedTasks, Set<Pair<MemoryModuleType<?>, MemoryModuleState>> requiredMemories, Set<MemoryModuleType<?>> forgettingMemories, CallbackInfo ci) {
        this.flatTasks = null;
    }

    @Inject(
            method = "clear()V",
            at = @At("RETURN")
    )
    private void clearTaskList(CallbackInfo ci) {
        this.flatTasks = null;
    }

    private void initTaskList() {
        ObjectArrayList<Task<? super E>> list = new ObjectArrayList<>();

        for (Map<Activity, Set<Task<? super E>>> map : this.tasks.values()) {
            for (Set<Task<? super E>> set : map.values()) {
                for (Task<? super E> task : set) {
                    //noinspection UseBulkOperation
                    list.add(task);
                }
            }
        }
        this.flatTasks = new MaskedList<>(list);
    }

    @Inject(
            method = "stopAllTasks",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/ai/brain/task/Task;stop(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LivingEntity;J)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void removeStoppedTask(ServerWorld world, E entity, CallbackInfo ci, long l, Iterator<?> it, Task<? super E> task) {
        if (this.flatTasks == null) {
            this.initTaskList();
        }
        this.flatTasks.setVisible(task, false);
    }

    @Inject(
            method = "updateTasks",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/ai/brain/task/Task;tick(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LivingEntity;J)V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void removeTaskIfStopped(ServerWorld world, E entity, CallbackInfo ci, long l, Iterator<?> it, Task<? super E> task) {
        if (task.getStatus() != Task.Status.RUNNING) {
            if (this.flatTasks == null) {
                this.initTaskList();
            }
            this.flatTasks.setVisible(task, false);
        }
    }

    @ModifyVariable(
            method = "startTasks",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/ai/brain/task/Task;tryStarting(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LivingEntity;J)Z",
                    shift = At.Shift.AFTER
            )
    )
    private Task<? super E> addStartedTasks(Task<? super E> task) {
        if (task.getStatus() == Task.Status.RUNNING) {
            if (this.flatTasks == null) {
                this.initTaskList();
            }
            this.flatTasks.setVisible(task, true);
        }
        return task;
    }

    /**
     * @author 2No2Name
     * @reason keep the list updated instead of recreating it all the time
     */
    @Overwrite
    @Deprecated
    @Debug
    public List<Task<? super E>> getRunningTasks() {
        if (this.flatTasks == null) {
            this.initTaskList();
        }
        return this.flatTasks;
    }

}
