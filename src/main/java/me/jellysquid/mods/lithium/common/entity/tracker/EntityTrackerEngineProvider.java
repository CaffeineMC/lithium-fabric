package me.jellysquid.mods.lithium.common.entity.tracker;

public interface EntityTrackerEngineProvider {
    EntityTrackerEngine getEntityTracker();

    static EntityTrackerEngine getEntityTracker(Object world) {
        return world instanceof EntityTrackerEngineProvider ? ((EntityTrackerEngineProvider) world).getEntityTracker() : null;
    }
}
