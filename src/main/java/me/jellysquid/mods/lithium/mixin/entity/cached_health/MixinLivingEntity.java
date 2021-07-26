package me.jellysquid.mods.lithium.mixin.entity.cached_health;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
    private long lastHealthUpdate = -1;
    private float cachedHealth = 1f;

    private MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "getHealth", cancellable = true, at = @At("HEAD"))
    private void useCacheGetHealth(CallbackInfoReturnable<Float> cir) {
        long time = this.world.getTime();
        if (time == lastHealthUpdate) {
            cir.setReturnValue(cachedHealth);
        }
        lastHealthUpdate = time;
    }


    @Inject(method = "getHealth", at = @At("RETURN"))
    private void setCacheGetHealth(CallbackInfoReturnable<Float> cir) {
        cachedHealth = cir.getReturnValueF();
    }

    @Inject(method = "setHealth", at = @At("RETURN"))
    private void setCacheSetHealth(float health, CallbackInfo ci) {
        cachedHealth = health;
    }
}
