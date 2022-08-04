package me.jellysquid.mods.lithium.common.block.entity.inventory_change_tracking;

import net.minecraft.inventory.Inventory;

public interface InventoryChangeListener {
    void handleStackListReplaced(Inventory inventory);

    void handleInventoryContentModified(Inventory inventory);

    void handleInventoryRemoved(Inventory inventory);

    void handleComparatorAdded(Inventory inventory);
}
