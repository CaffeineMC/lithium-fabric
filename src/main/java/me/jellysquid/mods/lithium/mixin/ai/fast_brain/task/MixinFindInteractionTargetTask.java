package me.jellysquid.mods.lithium.mixin.ai.fast_brain.task;

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
public abstract class MixinFindInteractionTargetTask extends Task<LivingEntity> {
    @Shadow
    @Final
    private Predicate<LivingEntity> shouldRunPredicate;

    @Shadow
    protected abstract List<LivingEntity> getVisibleMobs(LivingEntity livingEntity_1);

    @Shadow
    protected abstract boolean test(LivingEntity livingEntity_1);

    @Shadow
    @Final
    private int maxSquaredDistance;

    public MixinFindInteractionTargetTask(Map<MemoryModuleType<?>, MemoryModuleState> memories) {
        super(memories);
    }

    /**
     * @reason Replace stream code with traditional iteration
     * @author JellySquid
     */
    @Overwrite
    public boolean shouldRun(ServerWorld world, LivingEntity self) {
        if (!this.shouldRunPredicate.test(self)) {
            return false;
        }

        List<LivingEntity> visible = this.getVisibleMobs(self);

        for (LivingEntity entity : visible) {
            if (this.test(entity)) {
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
    public void run(ServerWorld world, LivingEntity self, long time) {
        super.run(world, self, time);

        Brain<?> brain = self.getBrain();

        List<LivingEntity> visible = brain.getOptionalMemory(MemoryModuleType.VISIBLE_MOBS)
                .orElse(Collections.emptyList());

        for (LivingEntity entity : visible) {
            if (entity.squaredDistanceTo(self) > (double) this.maxSquaredDistance) {
                continue;
            }

            if (this.test(entity)) {
                brain.remember(MemoryModuleType.INTERACTION_TARGET, entity);
                brain.remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(entity));

                break;
            }
        }
    }

}
