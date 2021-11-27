package me.jellysquid.mods.lithium.common.entity.tracker.nearby;

import me.jellysquid.mods.lithium.common.util.tuples.Range6Int;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows for multiple listeners on an entity to be grouped under one logical listener. No guarantees are made about the
 * order of which each sub-listener will be notified.
 */
public class NearbyEntityListenerMulti implements NearbyEntityListener {
    private final List<NearbyEntityListener> listeners = new ArrayList<>(4);
    private Range6Int range = null;

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
    public Range6Int getChunkRange() {
        if (this.range != null) {
            return this.range;
        }
        return this.calculateRange();
    }
    private Range6Int calculateRange() {
        if (this.listeners.isEmpty()) {
            return this.range = EMPTY_RANGE;
        }
        int positiveX = -1;
        int positiveY = -1;
        int positiveZ = -1;
        int negativeX = 0;
        int negativeY = 0;
        int negativeZ = 0;

        for (NearbyEntityListener listener : this.listeners) {
            Range6Int chunkRange = listener.getChunkRange();
            positiveX = Math.max(chunkRange.positiveX(), positiveX);
            positiveY = Math.max(chunkRange.positiveY(), positiveY);
            positiveZ = Math.max(chunkRange.positiveZ(), positiveZ);
            negativeX = Math.max(chunkRange.negativeX(), negativeX);
            negativeY = Math.max(chunkRange.negativeY(), negativeY);
            negativeZ = Math.max(chunkRange.negativeZ(), negativeZ);

        }
        return this.range = new Range6Int(positiveX, positiveY, positiveZ, negativeX, negativeY, negativeZ);
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
