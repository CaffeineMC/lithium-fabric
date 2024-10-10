package net.caffeinemc.mods.lithium.mixin.entity.inactive_navigations;

import net.caffeinemc.mods.lithium.common.entity.NavigatingEntity;
import net.minecraft.world.entity.monster.Drowned;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Drowned.class)
public class DrownedMixin {
    @Inject(method = "updateSwimming()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Drowned;setSwimming(Z)V"))
    private void updateInactivityState(CallbackInfo ci) {
        ((NavigatingEntity) this).lithium$updateNavigationRegistration();
    }
}
