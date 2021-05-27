package me.jellysquid.mods.lithium.common.entity.tracker;

import me.jellysquid.mods.lithium.common.entity.tracker.nearby.NearbyEntityListener;
import me.jellysquid.mods.lithium.common.entity.tracker.nearby.NearbyEntityMovementTracker;

public interface EntityTrackerSection {
    void addListener(NearbyEntityListener listener);

    void removeListener(NearbyEntityListener listener);

    void addListener(NearbyEntityMovementTracker<?, ?> listener);

    void removeListener(NearbyEntityMovementTracker<?, ?> listener);

    void updateMovementTimestamps(int notificationMask, long time);

    long getMovementTimestamp(int index);

    void setPos(long chunkSectionPos);

    long getPos();
}
