package me.jellysquid.mods.lithium.common.world.layer;

public interface CachingLayerContextExtended {

    /**
     * Scrambles the local seed of the layer without calling an expensive floorMod, simulating a nextInt call with
     * less overhead.
     */
    void skipInt();
}