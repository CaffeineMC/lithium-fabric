package me.jellysquid.mods.lithium.common.entity.tracker.nearby;

import net.minecraft.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows for multiple listeners on an entity to be grouped under one logical listener. No guarantees are made about the
 * order of which each sub-listener will be notified.
 */
public class NearbyEntityListenerMulti implements NearbyEntityListener {
    private final List<NearbyEntityListener> listeners = new ArrayList<>();

    public void addListener(NearbyEntityListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(NearbyEntityListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public int getChunkRange() {
        int range = 0;

        for (NearbyEntityListener listener : this.listeners) {
            range = Math.max(range, listener.getChunkRange());
        }

        return range;
    }

    @Override
    public void onEntityEnteredRange(LivingEntity entity) {
        for (NearbyEntityListener listener : this.listeners) {
            listener.onEntityEnteredRange(entity);
        }
    }

    @Override
    public void onEntityLeftRange(LivingEntity entity) {
        for (NearbyEntityListener listener : this.listeners) {
            listener.onEntityLeftRange(entity);
        }
    }

}
