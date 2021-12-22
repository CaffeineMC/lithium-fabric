package me.jellysquid.mods.lithium.mixin.collections.brain;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@Mixin(Brain.class)
public class BrainMixin {

    @Mutable
    @Shadow
    @Final
    private Map<?, ?> memories;

    @Mutable
    @Shadow
    @Final
    private Map<?, ?> sensors;

    @Mutable
    @Shadow
    @Final
    private Map<Integer, Map<Activity, Set<Task<?>>>> tasks;

    private ArrayList<Map<Activity, Set<Task<?>>>> tasksSorted;

    @Inject(
            method = "<init>(Ljava/util/Collection;Ljava/util/Collection;Lcom/google/common/collect/ImmutableList;Ljava/util/function/Supplier;)V",
            at = @At("RETURN")
    )
    private void reinitializeBrainCollections(Collection<?> memories, Collection<?> sensors, ImmutableList<?> memoryEntries, Supplier<?> codecSupplier, CallbackInfo ci) {
        this.memories = new Reference2ReferenceOpenHashMap<>(this.memories);
        this.sensors = new Reference2ReferenceLinkedOpenHashMap<>(this.sensors);
        this.tasksSorted = new ArrayList<>(this.tasks.values());
    }

    @Inject(
            method = "setTaskList(Lnet/minecraft/entity/ai/brain/Activity;Lcom/google/common/collect/ImmutableList;Ljava/util/Set;Ljava/util/Set;)V",
            at = @At("RETURN")
    )
    private void reinitializeTasksSorted(Activity activity, ImmutableList<? extends Pair<Integer, ? extends Task<?>>> indexedTasks, Set<Pair<MemoryModuleType<?>, MemoryModuleState>> requiredMemories, Set<MemoryModuleType<?>> forgettingMemories, CallbackInfo ci) {
        this.tasksSorted = new ArrayList<>(this.tasks.values());
    }

    @Inject(
            method = "clear()V",
            at = @At("RETURN")
    )
    private void reinitializeTasksSorted(CallbackInfo ci) {
        this.tasksSorted = new ArrayList<>(this.tasks.values());
    }

    @Redirect(
            method = "startTasks(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;values()Ljava/util/Collection;"
            )
    )
    private Collection<Map<Activity, Set<Task<?>>>> getTasksSorted(Map<?, ?> tasksTree) {
        return this.tasksSorted;
    }
}
