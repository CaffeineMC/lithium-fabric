package me.jellysquid.mods.lithium.mixin.ai.task.memory_change_counting;

import me.jellysquid.mods.lithium.common.ai.MemoryModificationCounter;
import net.minecraft.entity.ai.brain.Brain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;
import java.util.Optional;

@Mixin(Brain.class)
public class BrainMixin implements MemoryModificationCounter {

    private long memoryModCount = 1;

    @Redirect(
            method = "setMemory(Lnet/minecraft/entity/ai/brain/MemoryModuleType;Ljava/util/Optional;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
            )
    )
    private Object increaseMemoryModificationCount(Map<Object, Object> map, Object key, Object newValue) {
        Object oldValue = map.put(key, newValue);
        if (oldValue == null || ((Optional<?>) oldValue).isPresent() != ((Optional<?>) newValue).isPresent()) {
            this.memoryModCount++;
        }
        return oldValue;
    }

    @Override
    public long getModCount() {
        return memoryModCount;
    }
}
