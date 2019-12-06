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
     * set the cooldown of the hopper
     * @param cooldown vanilla sets it to 8 each operation
     */
    void setCooldown(int cooldown);

    /**
     * get the cooldown of the hopper
     * @return
     */
    int getCooldown();

    default boolean shouldAcceptItems() {
        // entities are ticked before block entities, so we need to check if the cooldown is less than equal to one rather than 0
        return this.getCooldown() <= 1 && this.enabled();
    }
}
