package me.jellysquid.mods.lithium.common.entity.movement_tracker;

import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySectionStorage;

public interface EntityMovementTrackerSection {
    void lithium$addListener(SectionedEntityMovementTracker<?, ?> listener);

    void lithium$removeListener(EntitySectionStorage<?> sectionedEntityCache, SectionedEntityMovementTracker<?, ?> listener);

    void lithium$trackEntityMovement(int notificationMask, long time);

    long lithium$getChangeTime(int trackedClass);

    <S, E extends EntityAccess> void lithium$listenToMovementOnce(SectionedEntityMovementTracker<E, S> listener, int trackedClass);

    <S, E extends EntityAccess> void lithium$removeListenToMovementOnce(SectionedEntityMovementTracker<E, S> listener, int trackedClass);
}
