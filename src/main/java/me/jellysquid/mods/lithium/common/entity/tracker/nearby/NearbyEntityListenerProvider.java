package me.jellysquid.mods.lithium.common.entity.tracker.nearby;

import org.jetbrains.annotations.Nullable;

public interface NearbyEntityListenerProvider {
    @Nullable
    NearbyEntityListenerMulti getListener();

    void addListener(NearbyEntityListener listener);
}
