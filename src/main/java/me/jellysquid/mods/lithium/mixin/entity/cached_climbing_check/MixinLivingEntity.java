package me.jellysquid.mods.lithium.mixin.entity.cached_climbing_check;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
    private long lastClimbingUpdate;
    private boolean cachedClimbing = false;

    private MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Redirect(method = "isPushable", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isClimbing()Z"))
    boolean preventClimbingUpdateCheck(LivingEntity livingEntity) {
        long time = livingEntity.world.getTime();
        if (time != lastClimbingUpdate) {
            lastClimbingUpdate = time;
            cachedClimbing = livingEntity.isClimbing();
        }
        return cachedClimbing;
    }
}
