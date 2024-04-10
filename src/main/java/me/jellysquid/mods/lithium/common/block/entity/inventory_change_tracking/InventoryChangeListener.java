package me.jellysquid.mods.lithium.common.block.entity.inventory_change_tracking;

import net.minecraft.inventory.Inventory;

public interface InventoryChangeListener {
    default void handleStackListReplaced(Inventory inventory) {
        this.lithium$handleInventoryRemoved(inventory);
    }

    void lithium$handleInventoryContentModified(Inventory inventory);

    void lithium$handleInventoryRemoved(Inventory inventory);

    boolean lithium$handleComparatorAdded(Inventory inventory);
}
