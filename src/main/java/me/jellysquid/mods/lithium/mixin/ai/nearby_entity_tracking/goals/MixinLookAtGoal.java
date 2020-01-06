package me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking.goals;

import me.jellysquid.mods.lithium.common.entity.tracker.nearby.EntityWithNearbyListener;
import me.jellysquid.mods.lithium.common.entity.tracker.nearby.NearbyEntityTracker;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LookAtEntityGoal.class)
public class MixinLookAtGoal {
    private NearbyEntityTracker<? extends LivingEntity> tracker;

    @Inject(method = "<init>(Lnet/minecraft/entity/mob/MobEntity;Ljava/lang/Class;FF)V", at = @At("RETURN"))
    private void init(MobEntity mob, Class<? extends LivingEntity> targetType, float range, float chance, CallbackInfo ci) {
        this.tracker = new NearbyEntityTracker<>(targetType, mob, range);

        ((EntityWithNearbyListener) mob).getListener().addListener(this.tracker);
    }

    @Redirect(method = "canStart", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getClosestEntityIncludingUngeneratedChunks(Ljava/lang/Class;Lnet/minecraft/entity/ai/TargetPredicate;Lnet/minecraft/entity/LivingEntity;DDDLnet/minecraft/util/math/Box;)Lnet/minecraft/entity/LivingEntity;"))
    private <T extends LivingEntity> LivingEntity redirectGetClosestEntity(World world, Class<? extends T> entityClass, TargetPredicate targetPredicate, LivingEntity entity, double x, double y, double z, Box box) {
        return this.tracker.getClosestEntity();
    }

    @Redirect(method = "canStart", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getClosestPlayer(Lnet/minecraft/entity/ai/TargetPredicate;Lnet/minecraft/entity/LivingEntity;DDD)Lnet/minecraft/entity/player/PlayerEntity;"))
    private PlayerEntity redirectGetClosestPlayer(World world, TargetPredicate targetPredicate, LivingEntity entity, double x, double y, double z) {
        return (PlayerEntity) this.tracker.getClosestEntity();
    }
}
