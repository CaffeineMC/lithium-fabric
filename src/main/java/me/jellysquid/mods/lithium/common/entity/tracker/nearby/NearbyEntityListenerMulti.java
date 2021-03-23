package me.jellysquid.mods.lithium.common.entity.tracker.nearby;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows for multiple listeners on an entity to be grouped under one logical listener. No guarantees are made about the
 * order of which each sub-listener will be notified.
 */
public class NearbyEntityListenerMulti implements NearbyEntityListener {
    private final List<NearbyEntityListener> listeners = new ArrayList<>(4);
    private Vec3i range = null;

    public void addListener(NearbyEntityListener listener) {
        if (this.range != null) {
            throw new IllegalStateException("Cannot add sublisteners after listening range was set!");
        }
        this.listeners.add(listener);
    }

    public void removeListener(NearbyEntityListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public Vec3i getChunkRange() {
        Vec3i range = this.range;
        if (range != null) {
            return range;
        }

        if (this.listeners.isEmpty()) {
            return this.range = EMPTY_RANGE;
        }
        int xRange = -1, yRange = -1, zRange = -1;
        for (NearbyEntityListener listener : this.listeners) {
            Vec3i chunkRange = listener.getChunkRange();
            xRange = Math.max(chunkRange.getX(), xRange);
            yRange = Math.max(chunkRange.getY(), yRange);
            zRange = Math.max(chunkRange.getZ(), zRange);
        }
        assert xRange > 0 && yRange > 0 && zRange > 0;
        range = new Vec3i(xRange, yRange, zRange);
        return this.range = range;
    }

    @Override
    public void onEntityEnteredRange(Entity entity) {
        for (NearbyEntityListener listener : this.listeners) {
            listener.onEntityEnteredRange(entity);
        }
    }

    @Override
    public void onEntityLeftRange(Entity entity) {
        for (NearbyEntityListener listener : this.listeners) {
            listener.onEntityLeftRange(entity);
        }
    }

    @Override
    public String toString() {
        StringBuilder sublisteners = new StringBuilder();
        String comma = "";
        for (NearbyEntityListener listener : this.listeners) {
            sublisteners.append(comma).append(listener.toString());
            comma = ","; //trick to drop the first comma
        }

        return super.toString() + " with sublisteners: [" + sublisteners + "]";
    }
}
