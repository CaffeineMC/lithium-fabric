package me.jellysquid.mods.lithium.mixin.ai.task;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.BreedTask;
import net.minecraft.entity.passive.AnimalEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Mixin(BreedTask.class)
public class BreedTaskMixin {
    @Shadow
    @Final
    private EntityType<? extends AnimalEntity> targetType;

    /**
     * @reason Replace stream code with traditional iteration
     * @author Maity
     */
    @Overwrite
    private Optional<? extends AnimalEntity> findBreedTarget(AnimalEntity entity) {
        List<LivingEntity> visibleMobs = entity.getBrain().getOptionalMemory(MemoryModuleType.VISIBLE_MOBS)
                .orElse(Collections.emptyList());

        AnimalEntity ret = null;

        for (LivingEntity mob : visibleMobs) {
            if (mob.getType() != this.targetType) {
                continue;
            }

            AnimalEntity animal = (AnimalEntity) mob;

            if (entity.canBreedWith(animal)) {
                ret = animal;
                break;
            }
        }

        return Optional.ofNullable(ret);
    }
}
