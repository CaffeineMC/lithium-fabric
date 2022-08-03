package me.jellysquid.mods.lithium.mixin.util.inventory_change_listening;


import me.jellysquid.mods.lithium.api.inventory.LithiumInventory;
import me.jellysquid.mods.lithium.common.block.entity.inventory_change_tracking.InventoryChangeListener;
import me.jellysquid.mods.lithium.common.block.entity.inventory_change_tracking.InventoryChangeTracker;
import me.jellysquid.mods.lithium.common.hopper.InventoryHelper;
import me.jellysquid.mods.lithium.common.hopper.LithiumStackList;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;

@Mixin(LockableContainerBlockEntity.class)
public abstract class LockableContainerBlockEntityMixin implements InventoryChangeTracker, Inventory {
    ArrayList<InventoryChangeListener> inventoryChangeListeners = null;

    @Override
    public void emitStackListReplaced() {
        ArrayList<InventoryChangeListener> inventoryChangeListeners = this.inventoryChangeListeners;
        if (inventoryChangeListeners != null) {
            for (int i = inventoryChangeListeners.size() - 1; i >= 0; i--) {
                InventoryChangeListener inventoryChangeListener = inventoryChangeListeners.remove(i);
                inventoryChangeListener.handleStackListReplaced(this);
            }
        }
    }

    @Override
    public void emitRemoved() {
        ArrayList<InventoryChangeListener> inventoryChangeListeners = this.inventoryChangeListeners;
        if (inventoryChangeListeners != null) {
            for (int i = inventoryChangeListeners.size() - 1; i >= 0; i--) {
                InventoryChangeListener inventoryChangeListener = inventoryChangeListeners.remove(i);
                inventoryChangeListener.handleInventoryRemoved(this);
            }
        }
    }

    @Override
    public void emitContentModified() {
        ArrayList<InventoryChangeListener> inventoryChangeListeners = this.inventoryChangeListeners;
        if (inventoryChangeListeners != null) {
            for (int i = inventoryChangeListeners.size() - 1; i >= 0; i--) {
                InventoryChangeListener inventoryChangeListener = inventoryChangeListeners.remove(i);
                inventoryChangeListener.handleInventoryContentModified(this);
            }
        }
    }

    @Override
    public void emitComparatorAdded() {
        ArrayList<InventoryChangeListener> inventoryChangeListeners = this.inventoryChangeListeners;
        if (inventoryChangeListeners != null) {
            for (int i = inventoryChangeListeners.size() - 1; i >= 0; i--) {
                InventoryChangeListener inventoryChangeListener = inventoryChangeListeners.remove(i);
                inventoryChangeListener.handleComparatorAdded(this);
            }
        }
    }

    @Override
    public void listenOnce(InventoryChangeListener inventoryChangeListener) {
        if (this.inventoryChangeListeners == null) {
            this.inventoryChangeListeners = new ArrayList<>(1);
        }
        if (this.inventoryChangeListeners.isEmpty()) {
            LithiumStackList lithiumStackList = InventoryHelper.getLithiumStackList((LithiumInventory) this);
            lithiumStackList.setInventoryModificationCallback(this);
        }
        this.inventoryChangeListeners.add(inventoryChangeListener);

    }
}
