package me.jellysquid.mods.lithium.mixin.ai.task.fast_repetition;

import me.jellysquid.mods.lithium.common.util.collections.PredicateFilterableList;
import net.minecraft.entity.EntityType;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Mixin(FindInteractionTargetTask.class)
public abstract class FindInteractionTargetTaskMixin extends Task<LivingEntity> {

    @Shadow
    @Final
    private Predicate<LivingEntity> shouldRunPredicate;
    @Shadow
    @Final
    private int maxSquaredDistance;
    private Predicate<LivingEntity> typePredicate;
    private boolean predicateIsStable;
    private Collection<?> previousVisibleMobs;
    private boolean previousShouldRun;

    public FindInteractionTargetTaskMixin(Map<MemoryModuleType<?>, MemoryModuleState> requiredMemoryState) {
        super(requiredMemoryState);
    }

    @Shadow
    protected abstract List<LivingEntity> getVisibleMobs(LivingEntity entity);

    @Shadow
    protected abstract boolean test(LivingEntity entity);

    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;ILjava/util/function/Predicate;Ljava/util/function/Predicate;)V",
            at = @At("RETURN"))
    private void initTypePredicate(EntityType<?> entityType, int maxDistance, Predicate<LivingEntity> shouldRunPredicate, Predicate<LivingEntity> predicate, CallbackInfo ci) {
        this.typePredicate = (LivingEntity livingEntity) -> livingEntity.getType() == entityType;
    }

    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;I)V",
            at = @At("RETURN"))
    private void init(EntityType<?> entityType, int maxDistance, CallbackInfo ci) {
        this.predicateIsStable = true;
    }

    /**
     * @reason Cache repeated type checks
     * @author 2No2Name
     */
    @Overwrite
    public boolean shouldRun(ServerWorld world, LivingEntity entity) {
        if (!this.shouldRunPredicate.test(entity)) {
            return false;
        }

        List<LivingEntity> visibleEntities = this.getVisibleMobs(entity);
        if (this.predicateIsStable) {
            if (visibleEntities == this.previousVisibleMobs) {
                return this.previousShouldRun;
            }
            this.previousVisibleMobs = visibleEntities;
        }

        int upperBound = visibleEntities.size();
        if (this.predicateIsStable && visibleEntities instanceof PredicateFilterableList<LivingEntity> predicateFilteredEntities) {
            visibleEntities = predicateFilteredEntities.getFiltered(this.typePredicate);
        }
        for (int i = 0; i < upperBound; i++) {
            LivingEntity visibleEntity = visibleEntities.get(i);
            if (visibleEntity == null) {
                return this.previousShouldRun = false;
            }
            if (this.test(visibleEntity)) {
                return this.previousShouldRun = true;
            }
        }
        return this.previousShouldRun = false;
    }

    /**
     * @reason Cache repeated type checks
     * @author 2No2Name
     */
    @Overwrite
    public void run(ServerWorld world, LivingEntity entity, long time) {
        super.run(world, entity, time);

        Brain<?> brain = entity.getBrain();
        Optional<List<LivingEntity>> visibleEntitiesMemory = brain.getOptionalMemory(MemoryModuleType.VISIBLE_MOBS);
        if (visibleEntitiesMemory.isPresent()) {
            List<LivingEntity> visibleEntities = visibleEntitiesMemory.get();

            int upperBound = visibleEntities.size();
            if (this.predicateIsStable && visibleEntities instanceof PredicateFilterableList<LivingEntity> predicateFilteredEntities) {
                visibleEntities = predicateFilteredEntities.getFiltered(this.typePredicate);
            }
            for (int i = 0; i < upperBound; i++) {
                LivingEntity visibleEntity = visibleEntities.get(i);
                if (visibleEntity == null) {
                    return;
                }
                if (visibleEntity.squaredDistanceTo(entity) <= (double) this.maxSquaredDistance &&
                        this.test(visibleEntity)) {
                    brain.remember(MemoryModuleType.INTERACTION_TARGET, visibleEntity);
                    brain.remember(MemoryModuleType.LOOK_TARGET, (new EntityLookTarget(visibleEntity, true)));
                    return;
                }
            }
        }
    }
}
