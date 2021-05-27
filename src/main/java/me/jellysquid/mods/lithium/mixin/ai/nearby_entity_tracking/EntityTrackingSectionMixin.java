package me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import me.jellysquid.mods.lithium.common.entity.tracker.EntityTrackerEngine;
import me.jellysquid.mods.lithium.common.entity.tracker.EntityTrackerSection;
import me.jellysquid.mods.lithium.common.entity.tracker.nearby.NearbyEntityMovementTracker;
import me.jellysquid.mods.lithium.common.entity.tracker.nearby.NearbyEntityListener;
import net.minecraft.entity.Entity;
import net.minecraft.util.collection.TypeFilterableList;
import net.minecraft.world.entity.EntityTrackingSection;
import net.minecraft.world.entity.EntityTrackingStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityTrackingSection.class)
public abstract class EntityTrackingSectionMixin<T> implements EntityTrackerSection {
    @Shadow
    private EntityTrackingStatus status;
    @Shadow
    @Final
    private TypeFilterableList<T> collection;

    private final ReferenceOpenHashSet<NearbyEntityListener> nearbyEntityListeners = new ReferenceOpenHashSet<>(0);
    private final ReferenceOpenHashSet<NearbyEntityMovementTracker<?, ?>> movementListeners = new ReferenceOpenHashSet<>(0);
    private final long[] lastEntityMovementByType = new long[EntityTrackerEngine.NUM_MOVEMENT_NOTIFYING_CLASSES];
    private long pos;

    @Override
    public void addListener(NearbyEntityListener listener) {
        this.nearbyEntityListeners.add(listener);
        if (this.status.shouldTrack()) {
            listener.onSectionEnteredRange(this, this.collection);
        }
    }

    @Override
    public void removeListener(NearbyEntityListener listener) {
        this.nearbyEntityListeners.remove(listener);
        if (this.status.shouldTrack()) {
            listener.onSectionLeftRange(this, this.collection);
        }
    }

    @Override
    public void addListener(NearbyEntityMovementTracker<?, ?> listener) {
        this.movementListeners.add(listener);
        if (this.status.shouldTrack()) {
            listener.onSectionEnteredRange(this);
        }
    }

    @Override
    public void removeListener(NearbyEntityMovementTracker<?, ?> listener) {
        this.movementListeners.remove(listener);
        if (this.status.shouldTrack()) {
            listener.onSectionLeftRange(this);
        }
    }

    @Override
    public void updateMovementTimestamps(int notificationMask, long time) {
        int size = this.lastEntityMovementByType.length;
        int mask;
        for (int i = Integer.numberOfTrailingZeros(notificationMask); i < size; i++) {
            this.lastEntityMovementByType[i] = time;
            mask = 0xffff_fffe << i;
            i = Integer.numberOfTrailingZeros(notificationMask & mask);
        }
    }

    public long getMovementTimestamp(int index) {
        return this.lastEntityMovementByType[index];
    }

    public void setPos(long chunkSectionPos) {
        this.pos = chunkSectionPos;
    }

    public long getPos() {
        return this.pos;
    }


    @Inject(method = "isEmpty", at = @At(value = "HEAD"), cancellable = true)
    public void isEmpty(CallbackInfoReturnable<Boolean> cir) {
        if (!this.nearbyEntityListeners.isEmpty()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "add", at = @At("RETURN"))
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

    @Inject(method = "remove", at = @At("RETURN"))
    private void onEntityRemoved(T entityLike, CallbackInfoReturnable<Boolean> cir) {
        if (this.status.shouldTrack() && entityLike instanceof Entity entity) {
            for (NearbyEntityListener nearbyEntityListener : this.nearbyEntityListeners) {
                nearbyEntityListener.onEntityLeftRange(entity);
            }
        }
    }

    @ModifyVariable(method = "swapStatus", at = @At(value = "HEAD"), argsOnly = true)
    public EntityTrackingStatus swapStatus(final EntityTrackingStatus newStatus) {
        if (this.status.shouldTrack() != newStatus.shouldTrack()) {
            if (!newStatus.shouldTrack()) {
                for (NearbyEntityListener nearbyEntityListener : this.nearbyEntityListeners) {
                    nearbyEntityListener.onSectionLeftRange(this, this.collection);
                }
                for (NearbyEntityMovementTracker<?, ?> listener : this.movementListeners) {
                    listener.onSectionLeftRange(this);
                }
            } else {
                for (NearbyEntityListener nearbyEntityListener : this.nearbyEntityListeners) {
                    nearbyEntityListener.onSectionEnteredRange(this, this.collection);
                }
                for (NearbyEntityMovementTracker<?, ?> listener : this.movementListeners) {
                    listener.onSectionEnteredRange(this);
                }
            }
        }
        return newStatus;
    }
}