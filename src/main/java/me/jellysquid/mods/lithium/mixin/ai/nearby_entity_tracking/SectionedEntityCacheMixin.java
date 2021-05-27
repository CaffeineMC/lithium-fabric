package me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking;

import me.jellysquid.mods.lithium.common.entity.tracker.EntityTrackerEngine;
import net.minecraft.world.entity.EntityTrackingSection;
import net.minecraft.world.entity.SectionedEntityCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SectionedEntityCache.class)
public class SectionedEntityCacheMixin {
    @Inject(method = "addSection", at = @At("RETURN"))
    private void rememberPos(long sectionPos, CallbackInfoReturnable<EntityTrackingSection<?>> cir) {
        ((EntityTrackerEngine.EntityTrackingSectionAccessor)cir.getReturnValue()).setPos(sectionPos);
    }
}
