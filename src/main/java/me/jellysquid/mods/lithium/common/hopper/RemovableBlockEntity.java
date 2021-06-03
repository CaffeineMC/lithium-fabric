package me.jellysquid.mods.lithium.common.hopper;

public interface RemovableBlockEntity {
    int getRemovedCountLithium(); //usages through LithiumInventory

    void increaseRemoveCounter();
}
