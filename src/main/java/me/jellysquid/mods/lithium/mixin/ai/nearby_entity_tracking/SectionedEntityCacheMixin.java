package me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import me.jellysquid.mods.lithium.common.entity.tracker.EntityTrackerSection;
import me.jellysquid.mods.lithium.common.entity.tracker.nearby.MovementTrackerCache;
import me.jellysquid.mods.lithium.common.entity.tracker.nearby.SectionedEntityMovementTracker;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityTrackingSection;
import net.minecraft.world.entity.SectionedEntityCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SectionedEntityCache.class)
public class SectionedEntityCacheMixin<T extends EntityLike> implements MovementTrackerCache {

    private final Object2ReferenceOpenHashMap<SectionedEntityMovementTracker<?, ?>, SectionedEntityMovementTracker<?, ?>> sectionEntityMovementTrackers = new Object2ReferenceOpenHashMap<>();

    @Inject(method = "addSection(J)Lnet/minecraft/world/entity/EntityTrackingSection;", at = @At("RETURN"))
    private void rememberPos(long sectionPos, CallbackInfoReturnable<EntityTrackingSection<T>> cir) {
        ((EntityTrackerSection) cir.getReturnValue()).setPos(sectionPos);
    }

    @Override
    public void remove(SectionedEntityMovementTracker<?, ?> tracker) {
        this.sectionEntityMovementTrackers.remove(tracker);
    }

    @Override
    public <S extends SectionedEntityMovementTracker<?, ?>> S deduplicate(S tracker) {
        //noinspection unchecked
        S storedTracker = (S) this.sectionEntityMovementTrackers.putIfAbsent(tracker, tracker);
        return storedTracker == null ? tracker : storedTracker;
    }
}
