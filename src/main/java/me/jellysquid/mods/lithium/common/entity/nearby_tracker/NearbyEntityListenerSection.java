package me.jellysquid.mods.lithium.common.entity.nearby_tracker;

import net.minecraft.world.entity.SectionedEntityCache;

public interface NearbyEntityListenerSection {
    void lithium$addListener(NearbyEntityListener listener);

    void lithium$removeListener(SectionedEntityCache<?> sectionedEntityCache, NearbyEntityListener listener);
}
