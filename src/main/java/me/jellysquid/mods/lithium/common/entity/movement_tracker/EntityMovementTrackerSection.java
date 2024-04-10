package me.jellysquid.mods.lithium.common.entity.movement_tracker;

import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.SectionedEntityCache;

public interface EntityMovementTrackerSection {
    void lithium$addListener(SectionedEntityMovementTracker<?, ?> listener);

    void lithium$removeListener(SectionedEntityCache<?> sectionedEntityCache, SectionedEntityMovementTracker<?, ?> listener);

    void lithium$trackEntityMovement(int notificationMask, long time);

    long lithium$getChangeTime(int trackedClass);

    <S, E extends EntityLike> void lithium$listenToMovementOnce(SectionedEntityMovementTracker<E, S> listener, int trackedClass);

    <S, E extends EntityLike> void lithium$removeListenToMovementOnce(SectionedEntityMovementTracker<E, S> listener, int trackedClass);
}
