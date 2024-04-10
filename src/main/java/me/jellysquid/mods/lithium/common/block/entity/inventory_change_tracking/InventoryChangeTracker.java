package me.jellysquid.mods.lithium.common.block.entity.inventory_change_tracking;

import me.jellysquid.mods.lithium.common.hopper.LithiumStackList;

public interface InventoryChangeTracker extends InventoryChangeEmitter {
    default void listenForContentChangesOnce(LithiumStackList stackList, InventoryChangeListener inventoryChangeListener) {
        this.lithium$forwardContentChangeOnce(inventoryChangeListener, stackList, this);
    }

    default void listenForMajorInventoryChanges(InventoryChangeListener inventoryChangeListener) {
        this.lithium$forwardMajorInventoryChanges(inventoryChangeListener);
    }

    default void stopListenForMajorInventoryChanges(InventoryChangeListener inventoryChangeListener) {
        this.lithium$stopForwardingMajorInventoryChanges(inventoryChangeListener);
    }
}
