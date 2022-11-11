package me.jellysquid.mods.lithium.common.hopper;

public interface RemovalCounter {
    int getRemovedCountLithium(); //usages through LithiumInventory

    default void increaseRemovedCounter() {
    }
}
