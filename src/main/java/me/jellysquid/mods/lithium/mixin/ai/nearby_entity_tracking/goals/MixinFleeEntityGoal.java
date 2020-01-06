package me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking.goals;

import me.jellysquid.mods.lithium.common.entity.tracker.nearby.EntityWithNearbyListener;
import me.jellysquid.mods.lithium.common.entity.tracker.nearby.NearbyEntityTracker;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.mob.MobEntityWithAi;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(FleeEntityGoal.class)
public class MixinFleeEntityGoal<T extends LivingEntity> {
    private NearbyEntityTracker<T> tracker;

    @Inject(method = "<init>(Lnet/minecraft/entity/mob/MobEntityWithAi;Ljava/lang/Class;Ljava/util/function/Predicate;FDDLjava/util/function/Predicate;)V", at = @At("RETURN"))
    private void init(MobEntityWithAi mob, Class<T> fleeFromType, Predicate<LivingEntity> predicate, float distance, double slowSpeed, double fastSpeed, Predicate<LivingEntity> predicate2, CallbackInfo ci) {
        this.tracker = new NearbyEntityTracker<>(fleeFromType, mob, distance);

        ((EntityWithNearbyListener) mob).getListener().addListener(this.tracker);
    }

    @Redirect(method = "canStart", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getClosestEntityIncludingUngeneratedChunks(Ljava/lang/Class;Lnet/minecraft/entity/ai/TargetPredicate;Lnet/minecraft/entity/LivingEntity;DDDLnet/minecraft/util/math/Box;)Lnet/minecraft/entity/LivingEntity;"))
    private T redirectGetNearestEntity(World world, Class<? extends T> entityClass, TargetPredicate targetPredicate, LivingEntity entity, double x, double y, double z, Box box) {
        return this.tracker.getClosestEntity();
    }
}
