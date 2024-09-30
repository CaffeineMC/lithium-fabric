package me.jellysquid.mods.lithium.mixin.util.entity_movement_tracking;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import me.jellysquid.mods.lithium.common.entity.PositionedEntityTrackingSection;
import me.jellysquid.mods.lithium.common.entity.movement_tracker.EntityMovementTrackerSection;
import me.jellysquid.mods.lithium.common.entity.movement_tracker.MovementTrackerHelper;
import me.jellysquid.mods.lithium.common.entity.movement_tracker.SectionedEntityMovementTracker;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.Visibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;

@Mixin(EntitySection.class)
public abstract class EntityTrackingSectionMixin implements EntityMovementTrackerSection, PositionedEntityTrackingSection {
    @Shadow
    private Visibility chunkStatus;

    @Shadow
    public abstract boolean isEmpty();

    @Unique
    private final ReferenceOpenHashSet<SectionedEntityMovementTracker<?, ?>> sectionVisibilityListeners = new ReferenceOpenHashSet<>(0);
    @Unique
    @SuppressWarnings("unchecked")
    private final ArrayList<SectionedEntityMovementTracker<?, ?>>[] entityMovementListenersByType = new ArrayList[MovementTrackerHelper.NUM_MOVEMENT_NOTIFYING_CLASSES];
    @Unique
    private final long[] lastEntityMovementByType = new long[MovementTrackerHelper.NUM_MOVEMENT_NOTIFYING_CLASSES];

    @Override
    public void lithium$addListener(SectionedEntityMovementTracker<?, ?> listener) {
        this.sectionVisibilityListeners.add(listener);
        if (this.chunkStatus.isAccessible()) {
            listener.onSectionEnteredRange(this);
        }
    }

    @Override
    public void lithium$removeListener(EntitySectionStorage<?> sectionedEntityCache, SectionedEntityMovementTracker<?, ?> listener) {
        boolean removed = this.sectionVisibilityListeners.remove(listener);
        if (this.chunkStatus.isAccessible() && removed) {
            listener.onSectionLeftRange(this);
        }
        if (this.isEmpty()) {
            sectionedEntityCache.remove(this.lithium$getPos());
        }
    }

    @Override
    public void lithium$trackEntityMovement(int notificationMask, long time) {
        long[] lastEntityMovementByType = this.lastEntityMovementByType;
        int size = lastEntityMovementByType.length;
        int mask;
        for (int entityClassIndex = Integer.numberOfTrailingZeros(notificationMask); entityClassIndex < size; ) {
            lastEntityMovementByType[entityClassIndex] = time;

            ArrayList<SectionedEntityMovementTracker<?, ?>> entityMovementListeners = this.entityMovementListenersByType[entityClassIndex];
            if (entityMovementListeners != null) {
                for (int listIndex = entityMovementListeners.size() - 1; listIndex >= 0; listIndex--) {
                    SectionedEntityMovementTracker<?, ?> sectionedEntityMovementTracker = entityMovementListeners.remove(listIndex);
                    sectionedEntityMovementTracker.emitEntityMovement(notificationMask, this);
                }
            }

            mask = 0xffff_fffe << entityClassIndex;
            entityClassIndex = Integer.numberOfTrailingZeros(notificationMask & mask);
        }
    }

    @Override
    public long lithium$getChangeTime(int trackedClass) {
        return this.lastEntityMovementByType[trackedClass];
    }

    @ModifyReturnValue(method = "isEmpty()Z", at = @At(value = "RETURN"))
    public boolean modifyIsEmpty(boolean previousIsEmpty) {
        return previousIsEmpty && this.sectionVisibilityListeners.isEmpty();
    }


    @ModifyVariable(method = "updateChunkStatus(Lnet/minecraft/world/level/entity/Visibility;)Lnet/minecraft/world/level/entity/Visibility;", at = @At(value = "HEAD"), argsOnly = true)
    public Visibility swapStatus(final Visibility newStatus) {
        if (this.chunkStatus.isAccessible() != newStatus.isAccessible()) {
            if (!newStatus.isAccessible()) {
                if (!this.sectionVisibilityListeners.isEmpty()) {
                    for (SectionedEntityMovementTracker<?, ?> listener : this.sectionVisibilityListeners) {
                        listener.onSectionLeftRange(this);
                    }
                }
            } else {
                if (!this.sectionVisibilityListeners.isEmpty()) {
                    for (SectionedEntityMovementTracker<?, ?> listener : this.sectionVisibilityListeners) {
                        listener.onSectionEnteredRange(this);
                    }
                }
            }
        }
        return newStatus;
    }

    @Override
    public <S, E extends EntityAccess> void lithium$listenToMovementOnce(SectionedEntityMovementTracker<E, S> listener, int trackedClass) {
        if (this.entityMovementListenersByType[trackedClass] == null) {
            this.entityMovementListenersByType[trackedClass] = new ArrayList<>();
        }
        this.entityMovementListenersByType[trackedClass].add(listener);
    }

    @Override
    public <S, E extends EntityAccess> void lithium$removeListenToMovementOnce(SectionedEntityMovementTracker<E, S> listener, int trackedClass) {
        if (this.entityMovementListenersByType[trackedClass] != null) {
            this.entityMovementListenersByType[trackedClass].remove(listener);
        }
    }
}
