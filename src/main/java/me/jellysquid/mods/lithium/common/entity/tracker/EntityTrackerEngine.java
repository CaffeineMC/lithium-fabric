package me.jellysquid.mods.lithium.common.entity.tracker;

import me.jellysquid.mods.lithium.common.entity.tracker.nearby.NearbyEntityListener;
import me.jellysquid.mods.lithium.common.util.LongObjObjConsumer;
import me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking.EntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.world.entity.EntityChangeListener;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityTrackingSection;
import net.minecraft.world.entity.SectionedEntityCache;

/**
 * Helps to track the entities within a world and provide notifications to listeners when a tracked entity enters or leaves a
 * watched area. This removes the necessity to constantly poll the world for nearby entities each tick and generally
 * provides a sizable boost to performance. //todo benchmark in 1.17 again
 */
public abstract class EntityTrackerEngine {
    public static final LongObjObjConsumer<NearbyEntityListener, SectionedEntityCache<? extends EntityLike>> enteredRangeConsumer =
            (pos, nearbyEntityListener, entityCache) -> {
                ((TrackedEntityList) entityCache.getTrackingSection(pos)).addListener(nearbyEntityListener);
            };
    public static final LongObjObjConsumer<NearbyEntityListener, SectionedEntityCache<? extends EntityLike>> leftRangeConsumer =
            (pos, nearbyEntityListener, entityCache) -> {
                EntityTrackingSection<?> trackingSection = entityCache.getTrackingSection(pos);
                ((EntityTrackerEngine.TrackedEntityList) trackingSection).removeListener(nearbyEntityListener);
                if (trackingSection.isEmpty()) {
                    entityCache.removeSection(pos);
                }
            };

    public interface TrackedEntityList {
        void addListener(NearbyEntityListener listener);

        void removeListener(NearbyEntityListener listener);

        void notifyExactListeners(Entity entity);
    }

    public interface TrackedEntityWrapper {
        void updateExactPositionTrackedCount(int change);
    }

    public static void startTrackingExactly(ExactPositionListener listener, Entity entity) {
        updateEntityExactPositionTrackerCount(entity, 1);
    }

    public static void stopTrackingExactly(ExactPositionListener listener, Entity entity) {
        updateEntityExactPositionTrackerCount(entity, -1);
    }

    private static void updateEntityExactPositionTrackerCount(Entity entity, int change) {
        EntityChangeListener entityChangeListener = ((EntityAccessor) entity).getEntityChangeListener();
        ((TrackedEntityWrapper) entityChangeListener).updateExactPositionTrackedCount(change);
    }
}
