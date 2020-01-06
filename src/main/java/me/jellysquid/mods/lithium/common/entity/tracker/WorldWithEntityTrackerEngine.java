package me.jellysquid.mods.lithium.common.entity.tracker;

public interface WorldWithEntityTrackerEngine {
    EntityTrackerEngine getEntityTracker();

    static EntityTrackerEngine getEntityTracker(Object world) {
        return world instanceof WorldWithEntityTrackerEngine ? ((WorldWithEntityTrackerEngine) world).getEntityTracker() : null;
    }
}
