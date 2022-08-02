package me.jellysquid.mods.lithium.common.block.entity.inventory_change_tracking;

import net.minecraft.inventory.Inventory;

public interface InventoryChangeListener {
    default void handleStackListReplaced(Inventory inventory) {
        this.handleInventoryContentModified(inventory);
    }

    void handleInventoryContentModified(Inventory inventory);

    void handleInventoryRemoved(Inventory inventory);

    void handleComparatorAdded(Inventory inventory);
}
