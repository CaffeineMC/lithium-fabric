package me.jellysquid.mods.lithium.common.entity.nearby;

import net.minecraft.entity.LivingEntity;

public interface NearbyEntityListener {
    /**
     * Returns the range (in blocks) of this listener. This must never change during the lifetime of the listener.
     */
    int getRange();

    /**
     * Called by the entity tracker when an entity enters the range of this listener.
     */
    void onEntityEnteredRange(LivingEntity entity);

    /**
     * Called by the entity tracker when an entity leaves the range of this listener.
     */
    void onEntityLeftRange(LivingEntity entity);
}
