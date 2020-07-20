package me.jellysquid.mods.lithium.mixin.ai.task;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.FindInteractionTargetTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Mixin(FindInteractionTargetTask.class)
public abstract class FindInteractionTargetTaskMixin extends Task<LivingEntity> {
    @Shadow
    @Final
    private Predicate<LivingEntity> shouldRunPredicate;

    @Shadow
    protected abstract List<LivingEntity> getVisibleMobs(LivingEntity entity);

    @Shadow
    protected abstract boolean test(LivingEntity entity);

    @Shadow
    @Final
    private int maxSquaredDistance;

    public FindInteractionTargetTaskMixin(Map<MemoryModuleType<?>, MemoryModuleState> memories) {
        super(memories);
    }

    /**
     * @reason Replace stream code with traditional iteration
     * @author JellySquid
     */
    @Overwrite
    public boolean shouldRun(ServerWorld world, LivingEntity entity) {
        if (!this.shouldRunPredicate.test(entity)) {
            return false;
        }

        List<LivingEntity> visibleEntities = this.getVisibleMobs(entity);

        for (LivingEntity otherEntity : visibleEntities) {
            if (this.test(otherEntity)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @reason Replace stream code with traditional iteration
     * @author JellySquid
     */
    @Overwrite
    public void run(ServerWorld world, LivingEntity entity, long time) {
        super.run(world, entity, time);

        Brain<?> brain = entity.getBrain();

        List<LivingEntity> visibleEntities = brain.getOptionalMemory(MemoryModuleType.VISIBLE_MOBS)
                .orElse(Collections.emptyList());

        for (LivingEntity otherEntity : visibleEntities) {
            if (otherEntity.squaredDistanceTo(entity) > (double) this.maxSquaredDistance) {
                continue;
            }

            if (this.test(otherEntity)) {
                brain.remember(MemoryModuleType.INTERACTION_TARGET, otherEntity);
                brain.remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(otherEntity, true));

                break;
            }
        }
    }

}
