package me.jellysquid.mods.lithium.common.entity.movement;

import me.jellysquid.mods.lithium.common.util.math.MutVec3d;

/**
 * Represents the push vector contributed from various source blocks of a given fluid. This is not meant to be used
 * by code outside the respective Mixin class, but we cannot class-load from the mixin package.
 */
public class FluidPushInfo {
    public MutVec3d velocity = new MutVec3d();
    public int sources;
    public double height;
}
