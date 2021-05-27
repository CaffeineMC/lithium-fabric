package me.jellysquid.mods.lithium.common.entity.tracker;

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import me.jellysquid.mods.lithium.common.entity.tracker.nearby.NearbyEntityListener;
import me.jellysquid.mods.lithium.common.entity.tracker.nearby.NearbyEntityMovementTracker;
import me.jellysquid.mods.lithium.common.util.LongObjObjConsumer;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityTrackingSection;
import net.minecraft.world.entity.SectionedEntityCache;

import java.util.List;

/**
 * Helps to track the entities within a world and provide notifications to listeners when a tracked entity enters or leaves a
 * watched area. This removes the necessity to constantly poll the world for nearby entities each tick and generally
 * provides a sizable boost to performance. //todo benchmark in 1.17 again
 */
public abstract class EntityTrackerEngine {
    public static final List<Class<?>> MOVEMENT_NOTIFYING_ENTITY_CLASSES;
    public static volatile Reference2IntOpenHashMap<Class<? extends EntityLike>> CLASS_2_NOTIFY_MASK;
    public static final int NUM_MOVEMENT_NOTIFYING_CLASSES;

    static {
        MOVEMENT_NOTIFYING_ENTITY_CLASSES = List.of(ItemEntity.class, Inventory.class);

        CLASS_2_NOTIFY_MASK = new Reference2IntOpenHashMap<>();
        CLASS_2_NOTIFY_MASK.defaultReturnValue(-1);
        NUM_MOVEMENT_NOTIFYING_CLASSES = MOVEMENT_NOTIFYING_ENTITY_CLASSES.size();
    }

    public static final LongObjObjConsumer<NearbyEntityListener, SectionedEntityCache<? extends EntityLike>> enteredRangeConsumer =
            (pos, nearbyEntityListener, entityCache) -> {
                ((EntityTrackingSectionAccessor) entityCache.getTrackingSection(pos)).addListener(nearbyEntityListener);
            };
    public static final LongObjObjConsumer<NearbyEntityListener, SectionedEntityCache<? extends EntityLike>> leftRangeConsumer =
            (pos, nearbyEntityListener, entityCache) -> {
                EntityTrackingSection<?> trackingSection = entityCache.getTrackingSection(pos);
                ((EntityTrackingSectionAccessor) trackingSection).removeListener(nearbyEntityListener);
                if (trackingSection.isEmpty()) {
                    entityCache.removeSection(pos);
                }
            };

    public static int getNotificationMask(Class<? extends EntityLike> entityClass) {
        int notificationMask = CLASS_2_NOTIFY_MASK.getInt(entityClass);
        if (notificationMask == -1) {
            notificationMask = calculateNotificationMask(entityClass);
        }
        return notificationMask;
    }
    private static int calculateNotificationMask(Class<? extends EntityLike> entityClass) {
        int mask = 0;
        for (int i = 0; i < MOVEMENT_NOTIFYING_ENTITY_CLASSES.size(); i++) {
            Class<?> superclass = MOVEMENT_NOTIFYING_ENTITY_CLASSES.get(i);
            if (superclass.isAssignableFrom(entityClass)) {
                mask |= 1 << i;
            }
        }

        //progress can be lost here, but it can only cost performance
        Reference2IntOpenHashMap<Class<? extends EntityLike>> copy = CLASS_2_NOTIFY_MASK.clone();
        copy.put(entityClass, mask);
        CLASS_2_NOTIFY_MASK = copy;

        return mask;
    }

    public interface EntityTrackingSectionAccessor {
        void addListener(NearbyEntityListener listener);

        void removeListener(NearbyEntityListener listener);

        void addListener(NearbyEntityMovementTracker<?, ?> listener);

        void removeListener(NearbyEntityMovementTracker<?, ?> listener);

        void updateMovementTimestamps(int notificationMask, long time);

        long getMovementTimestamp(int index);

        void setPos(long chunkSectionPos);

        long getPos();
    }
}
