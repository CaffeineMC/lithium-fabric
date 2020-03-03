package me.jellysquid.mods.lithium.mixin.ai.fast_brain.task;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.util.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(Task.class)
public class MixinTask<E extends LivingEntity> {
    @Shadow
    @Final
    protected Map<MemoryModuleType<?>, MemoryModuleState> requiredMemoryStates;

    private List<Pair<MemoryModuleType<?>, MemoryModuleState>> requiredMemoryStatesFlattened;

    @Inject(method = "<init>(Ljava/util/Map;II)V", at = @At("RETURN"))
    private void init(Map<MemoryModuleType<?>, MemoryModuleState> map, int int_1, int int_2, CallbackInfo ci) {
        List<Pair<MemoryModuleType<?>, MemoryModuleState>> flattened = new ArrayList<>(map.size());

        for (Map.Entry<MemoryModuleType<?>, MemoryModuleState> entry : this.requiredMemoryStates.entrySet()) {
            flattened.add(new Pair<>(entry.getKey(), entry.getValue()));
        }

        this.requiredMemoryStatesFlattened = flattened;
    }

    /**
     * @reason Replace stream-based code with traditional iteration, use a flattened array list to avoid pointer chasing
     * @author JellySquid
     */
    @Overwrite
    private boolean hasRequiredMemoryState(E entity) {
        for (Pair<MemoryModuleType<?>, MemoryModuleState> entry : this.requiredMemoryStatesFlattened) {
            if (!entity.getBrain().isMemoryInState(entry.getLeft(), entry.getRight())) {
                return false;
            }
        }

        return true;
    }
}
