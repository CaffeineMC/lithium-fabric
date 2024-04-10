package me.jellysquid.mods.lithium.common.shapes;

import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

public interface OffsetVoxelShapeCache {
    VoxelShape lithium$getOffsetSimplifiedShape(float offset, Direction direction);

    void lithium$setShape(float offset, Direction direction, VoxelShape offsetShape);
}