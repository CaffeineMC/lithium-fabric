package me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import me.jellysquid.mods.lithium.common.entity.tracker.EntityTrackerEngine;
import me.jellysquid.mods.lithium.common.entity.tracker.EntityTrackerSection;
import me.jellysquid.mods.lithium.common.entity.tracker.PositionedEntityTrackingSection;
import me.jellysquid.mods.lithium.common.entity.tracker.nearby.NearbyEntityListener;
import me.jellysquid.mods.lithium.common.entity.tracker.nearby.SectionedEntityMovementTracker;
import net.minecraft.entity.Entity;
import net.minecraft.util.collection.TypeFilterableList;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityTrackingSection;
import net.minecraft.world.entity.EntityTrackingStatus;
import net.minecraft.world.entity.SectionedEntityCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityTrackingSection.class)
public abstract class EntityTrackingSectionMixin<T extends EntityLike> implements EntityTrackerSection, PositionedEntityTrackingSection {
    @Shadow
    private EntityTrackingStatus status;
    @Shadow
    @Final
    private TypeFilterableList<T> collection;

    @Shadow
    public abstract boolean isEmpty();

    private final ReferenceOpenHashSet<NearbyEntityListener> nearbyEntityListeners = new ReferenceOpenHashSet<>(0);
    private final ReferenceOpenHashSet<SectionedEntityMovementTracker<?, ?>> sectionVisibilityListeners = new ReferenceOpenHashSet<>(0);
    private final long[] lastEntityMovementByType = new long[EntityTrackerEngine.NUM_MOVEMENT_NOTIFYING_CLASSES];

    @Override
    public void addListener(NearbyEntityListener listener) {
        this.nearbyEntityListeners.add(listener);
        if (this.status.shouldTrack()) {
            listener.onSectionEnteredRange(this, this.collection);
        }
    }

    @Override
    public void removeListener(SectionedEntityCache<?> sectionedEntityCache, NearbyEntityListener listener) {
        boolean removed = this.nearbyEntityListeners.remove(listener);
        if (this.status.shouldTrack() && removed) {
            listener.onSectionLeftRange(this, this.collection);
        }
        if (this.isEmpty()) {
            sectionedEntityCache.removeSection(this.getPos());
        }
    }

    @Override
    public void addListener(SectionedEntityMovementTracker<?, ?> listener) {
        this.sectionVisibilityListeners.add(listener);
        if (this.status.shouldTrack()) {
            listener.onSectionEnteredRange(this);
        }
    }

    @Override
    public void removeListener(SectionedEntityCache<?> sectionedEntityCache, SectionedEntityMovementTracker<?, ?> listener) {
        boolean removed = this.sectionVisibilityListeners.remove(listener);
        if (this.status.shouldTrack() && removed) {
            listener.onSectionLeftRange(this);
        }
        if (this.isEmpty()) {
            sectionedEntityCache.removeSection(this.getPos());
        }
    }

    @Override
    public void updateMovementTimestamps(int notificationMask, long time) {
        long[] lastEntityMovementByType = this.lastEntityMovementByType;
        int size = lastEntityMovementByType.length;
        int mask;
        for (int i = Integer.numberOfTrailingZeros(notificationMask); i < size; ) {
            lastEntityMovementByType[i] = time;
            mask = 0xffff_fffe << i;
            i = Integer.numberOfTrailingZeros(notificationMask & mask);
        }
    }

    public long[] getMovementTimestampArray() {
        return this.lastEntityMovementByType;
    }

    @Inject(method = "isEmpty()Z", at = @At(value = "HEAD"), cancellable = true)
    public void isEmpty(CallbackInfoReturnable<Boolean> cir) {
        if (!this.nearbyEntityListeners.isEmpty() || !this.sectionVisibilityListeners.isEmpty()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "add(Lnet/minecraft/world/entity/EntityLike;)V", at = @At("RETURN"))
    private void onEntityAdded(T entityLike, CallbackInfo ci) {
        if (!this.status.shouldTrack() || this.nearbyEntityListeners.isEmpty()) {
            return;
        }
        if (entityLike instanceof Entity entity) {
            for (NearbyEntityListener nearbyEntityListener : this.nearbyEntityListeners) {
                nearbyEntityListener.onEntityEnteredRange(entity);
            }
        }
    }

    @ModifyVariable(method = "remove(Lnet/minecraft/world/entity/EntityLike;)Z", at = @At("RETURN"), argsOnly = true)
    private T onEntityRemoved(final T entityLike) {
        if (this.status.shouldTrack() && !this.nearbyEntityListeners.isEmpty() && entityLike instanceof Entity entity) {
            for (NearbyEntityListener nearbyEntityListener : this.nearbyEntityListeners) {
                nearbyEntityListener.onEntityLeftRange(entity);
            }
        }
        return entityLike;
    }

    @ModifyVariable(method = "swapStatus(Lnet/minecraft/world/entity/EntityTrackingStatus;)Lnet/minecraft/world/entity/EntityTrackingStatus;", at = @At(value = "HEAD"), argsOnly = true)
    public EntityTrackingStatus swapStatus(final EntityTrackingStatus newStatus) {
        if (this.status.shouldTrack() != newStatus.shouldTrack()) {
            if (!newStatus.shouldTrack()) {
                if (!this.nearbyEntityListeners.isEmpty()) {
                    for (NearbyEntityListener nearbyEntityListener : this.nearbyEntityListeners) {
                        nearbyEntityListener.onSectionLeftRange(this, this.collection);
                    }
                }
                if (!this.sectionVisibilityListeners.isEmpty()) {
                    for (SectionedEntityMovementTracker<?, ?> listener : this.sectionVisibilityListeners) {
                        listener.onSectionLeftRange(this);
                    }
                }
            } else {
                if (!this.nearbyEntityListeners.isEmpty()) {
                    for (NearbyEntityListener nearbyEntityListener : this.nearbyEntityListeners) {
                        nearbyEntityListener.onSectionEnteredRange(this, this.collection);
                    }
                }
                if (!this.sectionVisibilityListeners.isEmpty()) {
                    for (SectionedEntityMovementTracker<?, ?> listener : this.sectionVisibilityListeners) {
                        listener.onSectionEnteredRange(this);
                    }
                }
            }
        }
        return newStatus;
    }
}
