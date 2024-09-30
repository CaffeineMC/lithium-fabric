package me.jellysquid.mods.lithium.mixin.entity.fast_hand_swing;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow
    public boolean swinging;

    @Shadow
    public int swingTime;

    @Inject(
            method = "updateSwingTime()V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void skipGetDuration(CallbackInfo ci) {
        if (!this.swinging && this.swingTime == 0) {
            ci.cancel();
        }
    }
}
