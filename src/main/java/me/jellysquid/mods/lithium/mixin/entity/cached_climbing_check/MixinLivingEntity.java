package me.jellysquid.mods.lithium.mixin.entity.cached_climbing_check;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
    private long lastClimbingUpdate = -1;
    private boolean cachedClimbing = false;

    private MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "isClimbing", cancellable = true, at = @At("HEAD"))
    private void useCacheIsClimbing(CallbackInfoReturnable<Boolean> cir) {
        long time = this.world.getTime();
        if (time == lastClimbingUpdate) {
            cir.setReturnValue(cachedClimbing);
        }
        lastClimbingUpdate = time;
    }

    @Inject(method = "isClimbing", at = @At("RETURN"))
    private void setCacheIsClimbing(CallbackInfoReturnable<Boolean> cir) {
        cachedClimbing = cir.getReturnValueZ();
    }
}
