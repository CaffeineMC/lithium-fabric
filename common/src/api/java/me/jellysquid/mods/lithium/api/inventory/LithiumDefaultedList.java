package me.jellysquid.mods.lithium.api.inventory;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;

public interface LithiumDefaultedList {
    /**
     * Call this method when the behavior of
     * {@link net.minecraft.world.Container#canPlaceItem(int, ItemStack)}
     * {@link net.minecraft.world.WorldlyContainer#canPlaceItemThroughFace(int, ItemStack, Direction)}
     * {@link net.minecraft.world.WorldlyContainer#canTakeItemThroughFace(int, ItemStack, Direction)}
     * or similar functionality changed.
     * This method will not need to be called if this change in behavior is triggered by a change of the stack list contents.
     */
    void changedInteractionConditions();
}
