package me.jellysquid.mods.lithium.common.block.entity.inventory_change_tracking;

import net.minecraft.world.Container;

public interface InventoryChangeListener {
    default void handleStackListReplaced(Container inventory) {
        this.lithium$handleInventoryRemoved(inventory);
    }

    void lithium$handleInventoryContentModified(Container inventory);

    void lithium$handleInventoryRemoved(Container inventory);

    boolean lithium$handleComparatorAdded(Container inventory);
}
