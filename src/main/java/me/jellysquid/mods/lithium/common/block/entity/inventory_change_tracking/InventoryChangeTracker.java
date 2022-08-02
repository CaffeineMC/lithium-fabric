package me.jellysquid.mods.lithium.common.block.entity.inventory_change_tracking;

public interface InventoryChangeTracker {
    void emitStackListReplaced();

    void emitRemoved();

    void emitContentModified();

    void emitComparatorAdded();

    void listenOnce(InventoryChangeListener inventoryChangeListener);
}
