package me.jellysquid.mods.lithium.common.entity.movement;

import me.jellysquid.mods.lithium.common.util.math.MutVec3d;

public class FluidPushInfo {
    public MutVec3d velocity = new MutVec3d();
    public int sources;
    public double height;
}
