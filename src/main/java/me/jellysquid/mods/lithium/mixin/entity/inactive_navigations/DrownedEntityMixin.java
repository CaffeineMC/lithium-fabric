package me.jellysquid.mods.lithium.mixin.entity.inactive_navigations;

import me.jellysquid.mods.lithium.common.entity.NavigatingEntity;
import net.minecraft.world.entity.monster.Drowned;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Drowned.class)
public class DrownedEntityMixin {
    @Inject(method = "updateSwimming()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Drowned;setSwimming(Z)V"))
    private void updateInactivityState(CallbackInfo ci) {
        ((NavigatingEntity) this).lithium$updateNavigationRegistration();
    }
}
