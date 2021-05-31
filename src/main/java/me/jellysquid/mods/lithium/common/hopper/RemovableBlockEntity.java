package me.jellysquid.mods.lithium.common.hopper;

public interface RemovableBlockEntity {
    int getRemovedCount(); //usages through LithiumInventory

    void increaseRemoveCounter();
}
