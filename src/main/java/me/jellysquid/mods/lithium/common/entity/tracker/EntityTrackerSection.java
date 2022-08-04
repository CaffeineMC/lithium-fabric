package me.jellysquid.mods.lithium.common.entity.tracker;

import me.jellysquid.mods.lithium.common.entity.tracker.nearby.NearbyEntityListener;
import me.jellysquid.mods.lithium.common.entity.tracker.nearby.SectionedEntityMovementTracker;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.SectionedEntityCache;

public interface EntityTrackerSection {
    void addListener(NearbyEntityListener listener);

    void removeListener(SectionedEntityCache<?> sectionedEntityCache, NearbyEntityListener listener);

    void addListener(SectionedEntityMovementTracker<?, ?> listener);

    void removeListener(SectionedEntityCache<?> sectionedEntityCache, SectionedEntityMovementTracker<?, ?> listener);

    void trackEntityMovement(int notificationMask, long time);

    long[] getMovementTimestampArray();

    long getChangeTime(int trackedClass);

    <S, E extends EntityLike> void listenToMovementOnce(SectionedEntityMovementTracker<E, S> listener, int trackedClass);

    <S, E extends EntityLike> void removeListenToMovementOnce(SectionedEntityMovementTracker<E, S> listener, int trackedClass);
}
