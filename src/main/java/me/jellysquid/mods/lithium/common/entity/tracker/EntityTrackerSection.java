package me.jellysquid.mods.lithium.common.entity.tracker;

import me.jellysquid.mods.lithium.common.entity.tracker.nearby.NearbyEntityListener;
import me.jellysquid.mods.lithium.common.entity.tracker.nearby.NearbyEntityMovementTracker;
import net.minecraft.world.entity.SectionedEntityCache;

public interface EntityTrackerSection {
    void addListener(NearbyEntityListener listener);

    void removeListener(SectionedEntityCache<?> sectionedEntityCache, NearbyEntityListener listener);

    void addListener(NearbyEntityMovementTracker<?, ?> listener);

    void removeListener(SectionedEntityCache<?> sectionedEntityCache, NearbyEntityMovementTracker<?, ?> listener);

    void updateMovementTimestamps(int notificationMask, long time);

    long getMovementTimestamp(int index);

    void setPos(long chunkSectionPos);

    long getPos();
}
