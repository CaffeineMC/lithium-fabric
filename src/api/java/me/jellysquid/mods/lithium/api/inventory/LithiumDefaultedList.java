package me.jellysquid.mods.lithium.api.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

public interface LithiumDefaultedList {
    /**
     * Call this method when the behavior of
     * {@link net.minecraft.inventory.Inventory#isValid(int, ItemStack)}
     * {@link net.minecraft.inventory.SidedInventory#canInsert(int, ItemStack, Direction)}
     * {@link net.minecraft.inventory.SidedInventory#canExtract(int, ItemStack, Direction)}
     * or similar functionality changed.
     * This method will not need to be called if this change in behavior is triggered by a change of the stack list contents.
     */
    void changedInteractionConditions();

    /**
     * This method is an alternative to {@link #changedInteractionConditions()} that should only be used if detecting
     * when {@link #changedInteractionConditions()} needs to be called will cost a lot of performance.
     * This method will make hoppers assume that this inventory is
     * constantly changing, which essentially disables several optimizations.
     * However, it will probably still be slightly more performant than vanilla hopper code interacting with the same
     * inventory.
     */
    void setUnstableInteractionConditions();
}
