package me.jellysquid.mods.lithium.common.entity.nearby_tracker;

import org.jetbrains.annotations.Nullable;

public interface NearbyEntityListenerProvider {
    @Nullable
    NearbyEntityListenerMulti lithium$getListener();

    void lithium$addListener(NearbyEntityTracker listener);
}
