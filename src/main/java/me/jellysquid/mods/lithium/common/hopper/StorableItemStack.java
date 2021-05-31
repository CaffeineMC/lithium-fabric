package me.jellysquid.mods.lithium.common.hopper;

public interface StorableItemStack {
    void registerToInventory(LithiumStackList itemStacks);
    void unregisterFromInventory(LithiumStackList myInventoryList);
}
