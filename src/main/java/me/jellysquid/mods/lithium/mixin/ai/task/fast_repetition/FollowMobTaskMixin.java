package me.jellysquid.mods.lithium.mixin.ai.task.fast_repetition;

import me.jellysquid.mods.lithium.common.util.collections.PredicateFilterableList;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.FollowMobTask;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.Tag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Optimize goats and other mobs which use FollowMobTask. Having many goats in a flat area makes this AI Task become
 * very laggy, due to the constant recalculations.
 */
@Mixin(FollowMobTask.class)
public abstract class FollowMobTaskMixin {
    @Shadow
    @Final
    private Predicate<LivingEntity> predicate;

    @Shadow
    @Final
    private float maxDistanceSquared;

    private boolean predicateIsStable;

    private Collection<?> previousVisibleMobs;
    private boolean previousShouldRun;

    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;F)V", at = @At(value = "RETURN"))
    private void activateCachedFiltering(EntityType<?> entityType, float maxDistance, CallbackInfo ci) {
        this.predicateIsStable = true;
    }

    @Inject(method = "<init>(F)V", at = @At(value = "RETURN"))
    private void activateCachedFiltering2(float maxDistance, CallbackInfo ci) {
        this.predicateIsStable = true;
    }

    @Inject(method = "<init>(Lnet/minecraft/entity/SpawnGroup;F)V", at = @At(value = "RETURN"))
    private void activateCachedFiltering3(SpawnGroup group, float maxDistance, CallbackInfo ci) {
        this.predicateIsStable = true;
    }

    @Inject(method = "<init>(Lnet/minecraft/tag/Tag;F)V", at = @At(value = "RETURN"))
    private void activateCachedFiltering4(Tag<EntityType<?>> entityType, float maxDistance, CallbackInfo ci) {
        this.predicateIsStable = true;
    }

    /**
     * @reason Cache repeated type checks
     * @author 2No2Name
     */
    @Overwrite
    public boolean shouldRun(ServerWorld world, LivingEntity entity) {
        //noinspection OptionalGetWithoutIsPresent
        List<LivingEntity> visibleEntities = entity.getBrain().getOptionalMemory(MemoryModuleType.VISIBLE_MOBS).get();
        if (this.predicateIsStable) {
            if (visibleEntities == this.previousVisibleMobs) {
                return this.previousShouldRun;
            }
            this.previousVisibleMobs = visibleEntities;
        }

        int upperBound = visibleEntities.size();
        if (this.predicateIsStable && visibleEntities instanceof PredicateFilterableList<LivingEntity> predicateFilteredEntities) {
            visibleEntities = predicateFilteredEntities.getFiltered(this.predicate);
        }
        for (int i = 0; i < upperBound; i++) {
            LivingEntity visibleEntity = visibleEntities.get(i);
            if (visibleEntity == null) {
                return this.previousShouldRun = false;
            }
            if (this.predicate.test(visibleEntity)) {
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
        Brain<?> brain = entity.getBrain();
        Optional<List<LivingEntity>> visibleEntitiesMemory = brain.getOptionalMemory(MemoryModuleType.VISIBLE_MOBS);
        if (visibleEntitiesMemory.isPresent()) {
            List<LivingEntity> visibleEntities = visibleEntitiesMemory.get();

            int upperBound = visibleEntities.size();
            if (this.predicateIsStable && visibleEntities instanceof PredicateFilterableList<LivingEntity> predicateFilteredEntities) {
                visibleEntities = predicateFilteredEntities.getFiltered(this.predicate);
            }
            for (int i = 0; i < upperBound; i++) {
                LivingEntity visibleEntity = visibleEntities.get(i);
                if (visibleEntity == null) {
                    return;
                }
                if (visibleEntity.squaredDistanceTo(entity) <= (double) this.maxDistanceSquared &&
                        this.predicate.test(visibleEntity)) {
                    brain.remember(MemoryModuleType.LOOK_TARGET, (new EntityLookTarget(visibleEntity, true)));
                    return;
                }
            }
        }
    }
}
