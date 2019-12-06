package me.jellysquid.mods.lithium.common.blockentities;

/**
 * accessor for checking if hopper block entities are locked
 */
public interface HopperAccess {
    /**
     * @return true if the hopper is unlocked
     */
    boolean enabled();
}
