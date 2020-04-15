package me.jellysquid.mods.lithium.common.shapes;

import net.minecraft.util.math.Box;

public interface VoxelShapeExtended {
    boolean intersects(Box box, double x, double y, double z);
}
