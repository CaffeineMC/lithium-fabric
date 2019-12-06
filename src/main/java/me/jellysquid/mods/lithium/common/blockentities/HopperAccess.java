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
    void setCool(int cooldown);

    /**
     * get the ticks until the cooldown of the hopper from a previous action is over
     * the reason we don't use the normal counter is that it is reset back to 8 no matter what, it's more of a counter
     * rather than a cooldown
     *
     * @return 0 if the hopper is ready to accept items
     */
    int realCooldown();

    default boolean shouldAcceptItems() {
        return this.realCooldown() <= 1 && this.enabled();
    }
}
