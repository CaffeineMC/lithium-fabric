package me.jellysquid.mods.lithium.common.util;

import net.minecraft.world.entity.EquipmentSlot;

/**
 * Pre-initialized constants to avoid unnecessary allocations.
 */
public final class EquipmentSlotConstants {
    private EquipmentSlotConstants() {}

    public static final EquipmentSlot[] ALL = EquipmentSlot.values();
}
