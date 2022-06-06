package me.jellysquid.mods.lithium.common.hopper;

public interface StorableItemStack {
    void registerToInventory(LithiumStackList itemStacks, int slot);

    void unregisterFromInventory(LithiumStackList myInventoryList);

    void unregisterFromInventory(LithiumStackList stackList, int index);
}
