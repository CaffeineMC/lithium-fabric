package me.jellysquid.mods.lithium.mixin.ai.task.memory_change_counting;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import me.jellysquid.mods.lithium.common.ai.MemoryModificationCounter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(Behavior.class)
public class MultiTickTaskMixin<E extends LivingEntity> {
    @Mutable
    @Shadow
    @Final
    protected Map<MemoryModuleType<?>, MemoryStatus> entryCondition;

    @Unique
    private long cachedMemoryModCount = -1;
    @Unique
    private boolean cachedHasRequiredMemoryState;

    @Inject(method = "<init>(Ljava/util/Map;II)V", at = @At("RETURN"))
    private void init(Map<MemoryModuleType<?>, MemoryStatus> map, int int_1, int int_2, CallbackInfo ci) {
        this.entryCondition = new Reference2ObjectOpenHashMap<>(map);
    }

    /**
     * @reason Use cached required memory state test result if memory state is unchanged
     * @author 2No2Name
     */
    @Overwrite
    public boolean hasRequiredMemories(E entity) {
        Brain<?> brain = entity.getBrain();
        long modCount = ((MemoryModificationCounter) brain).lithium$getModCount();
        if (this.cachedMemoryModCount == modCount) {
            return this.cachedHasRequiredMemoryState;
        }
        this.cachedMemoryModCount = modCount;

        ObjectIterator<Reference2ObjectMap.Entry<MemoryModuleType<?>, MemoryStatus>> fastIterator = ((Reference2ObjectOpenHashMap<MemoryModuleType<?>, MemoryStatus>) this.entryCondition).reference2ObjectEntrySet().fastIterator();
        while (fastIterator.hasNext()) {
            Reference2ObjectMap.Entry<MemoryModuleType<?>, MemoryStatus> entry = fastIterator.next();
            if (!brain.checkMemory(entry.getKey(), entry.getValue())) {
                return this.cachedHasRequiredMemoryState = false;
            }
        }

        return this.cachedHasRequiredMemoryState = true;
    }
}
