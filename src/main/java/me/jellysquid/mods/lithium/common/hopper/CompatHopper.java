package me.jellysquid.mods.lithium.common.hopper;

import net.minecraft.inventory.Inventory;

public interface CompatHopper {
    default boolean mayNotTransferTo(Inventory inventory) {
        return false;
    }

    default boolean mayNotTransferFrom(Inventory inventory) {
        return false;
    }
}
