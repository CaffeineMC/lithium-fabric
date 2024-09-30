package me.jellysquid.mods.lithium.common.shapes;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface OffsetVoxelShapeCache {
    VoxelShape lithium$getOffsetSimplifiedShape(float offset, Direction direction);

    void lithium$setShape(float offset, Direction direction, VoxelShape offsetShape);
}