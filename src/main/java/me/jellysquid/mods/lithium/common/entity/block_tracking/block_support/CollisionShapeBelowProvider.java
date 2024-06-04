package me.jellysquid.mods.lithium.common.entity.block_tracking.block_support;

import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.Nullable;

public interface CollisionShapeBelowProvider {

    @Nullable VoxelShape lithium$getCollisionShapeBelow();
}
