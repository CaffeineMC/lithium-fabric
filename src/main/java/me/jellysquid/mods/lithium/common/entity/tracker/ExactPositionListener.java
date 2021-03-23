package me.jellysquid.mods.lithium.common.entity.tracker;

import me.jellysquid.mods.lithium.common.entity.tracker.nearby.NearbyEntityListener;
import net.minecraft.entity.Entity;

public interface ExactPositionListener extends NearbyEntityListener {
    /**
     * Method that is called every time an entity moves. This method may even be called for entities that
     * this tracker is not interested in {@link #canTrack(Entity)}.
     * @param entity the Entity that just changed its position
     */
    void onEntityMoved(Entity entity);

    void onStartTrackingEntity(Entity entity);

    void onStopTrackingEntity(Entity entity);

    /**
     * Filter method to avoid notifying a ton of listeners about entities they never care about.
     * This filter allows skipping calling {@link #onEntityMoved(Entity)} for uninteresting entities, but it is
     * expected that this filtering is not 100% effective.
     * @param entity the trackable entity
     * @return Whether this listener can be interested into this entity now or at any time in the future
     */
    default boolean canTrack(Entity entity) {
        return true;
    }

    @Override
    default void onEntityEnteredRange(Entity entity) {
        if (this.canTrack(entity)) {
            EntityTrackerEngine.startTrackingExactly(this, entity);
            this.onStartTrackingEntity(entity);
        }
    }

    @Override
    default void onEntityLeftRange(Entity entity) {
        if (this.canTrack(entity)) {
            EntityTrackerEngine.stopTrackingExactly(this, entity);
            this.onStopTrackingEntity(entity);
        }
    }
}
