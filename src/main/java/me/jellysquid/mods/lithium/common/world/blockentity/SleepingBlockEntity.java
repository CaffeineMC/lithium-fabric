package me.jellysquid.mods.lithium.common.world.blockentity;

public interface SleepingBlockEntity {
    default boolean canTickOnSide(boolean isClient) {
        return true;
    }
}
