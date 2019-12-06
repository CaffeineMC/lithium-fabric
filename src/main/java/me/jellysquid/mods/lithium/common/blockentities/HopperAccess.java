package me.jellysquid.mods.lithium.common.blockentities;

/**
 * accessor for checking if hopper block entities are locked
 */
public interface HopperAccess {
    /**
     * @return true if the hopper is unlocked
     */
    boolean enabled();

    /**
     * @return the "transferCooldown" of the hopper
     */
    int cooldown();

    /**
     * @return true if the hopper's cooldown is over
     */
    default boolean cooled() {
        return this.cooldown() <= 0;
    }

    /**
     * returns true if the hopper should be able to accept items
     * @return
     */
    default boolean shouldAcceptItems() {
        return this.cooled() && this.enabled();
    }
}
