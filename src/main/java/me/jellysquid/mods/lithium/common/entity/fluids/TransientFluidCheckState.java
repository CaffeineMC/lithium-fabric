package me.jellysquid.mods.lithium.common.entity.fluids;

import me.jellysquid.mods.lithium.common.util.math.MutableVec3d;

public class TransientFluidCheckState {
    public MutableVec3d totalVelocity = new MutableVec3d();
    public int totalSourceCount = 0;

    public double fluidHeight = 0.0D;
    public boolean touching;
}
