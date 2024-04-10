package me.jellysquid.mods.lithium.common.block.entity.inventory_comparator_tracking;

import net.minecraft.util.math.Direction;

public interface ComparatorTracker {
    void lithium$onComparatorAdded(Direction direction, int offset);

    boolean lithium$hasAnyComparatorNearby();
}
