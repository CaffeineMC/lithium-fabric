package me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import me.jellysquid.mods.lithium.common.entity.tracker.EntityTrackerEngine;
import me.jellysquid.mods.lithium.common.entity.tracker.ExactPositionListener;
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
public abstract class EntityTrackingSectionMixin<T> implements EntityTrackerEngine.TrackedEntityList {
    @Shadow
    public abstract boolean isEmpty();

    @Shadow
    private EntityTrackingStatus status;
    @Shadow
    @Final
    private TypeFilterableList<T> collection;

    private final ReferenceOpenHashSet<NearbyEntityListener> nearbyEntityListeners = new ReferenceOpenHashSet<>(0);
    private final ReferenceOpenHashSet<ExactPositionListener> exactPositionListeners = new ReferenceOpenHashSet<>(0);


    @Override
    public void addListener(NearbyEntityListener listener) {
        this.nearbyEntityListeners.add(listener);
        if (listener instanceof ExactPositionListener) {
            this.exactPositionListeners.add((ExactPositionListener) listener);
        }
        listener.onSectionEnteredRange(this.collection);
    }

    @Override
    public void removeListener(NearbyEntityListener listener) {
        this.nearbyEntityListeners.remove(listener);
        if (listener instanceof ExactPositionListener) {
            this.exactPositionListeners.remove(listener);
        }
        listener.onSectionLeftRange(this.collection);
    }

    @Override
    public void notifyExactListeners(Entity entity) {
        for (ExactPositionListener exactPositionListener : this.exactPositionListeners) {
            exactPositionListener.onEntityMoved(entity);
        }
    }

    @Inject(method = "isEmpty", at = @At(value = "HEAD"), cancellable = true)
    public void isEmpty(CallbackInfoReturnable<Boolean> cir) {
        if (!this.nearbyEntityListeners.isEmpty()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "add", at = @At("RETURN"))
    private void onEntityAdded(T entityLike, CallbackInfo ci) {
        if (this.nearbyEntityListeners.isEmpty()) {
            return;
        }
        if (entityLike instanceof Entity) {
            Entity entity = (Entity) entityLike;
            for (NearbyEntityListener nearbyEntityListener : this.nearbyEntityListeners) {
                nearbyEntityListener.onEntityEnteredRange(entity);
            }
        }
    }

    @Inject(method = "remove", at = @At("RETURN"))
    private void onEntityRemoved(T entityLike, CallbackInfoReturnable<Boolean> cir) {
        if (entityLike instanceof Entity) {
            Entity entity = (Entity) entityLike;
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
                    nearbyEntityListener.onSectionLeftRange(this.collection);
                }
            } else {
                for (NearbyEntityListener nearbyEntityListener : this.nearbyEntityListeners) {
                    nearbyEntityListener.onSectionEnteredRange(this.collection);
                }
            }
        }
        return newStatus;
    }
}
