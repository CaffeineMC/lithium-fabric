package net.caffeinemc.mods.lithium.common.block.entity.inventory_comparator_tracking;

import net.minecraft.core.Direction;

public interface ComparatorTracker {
    void lithium$onComparatorAdded(Direction direction, int offset);

    boolean lithium$hasAnyComparatorNearby();
}
