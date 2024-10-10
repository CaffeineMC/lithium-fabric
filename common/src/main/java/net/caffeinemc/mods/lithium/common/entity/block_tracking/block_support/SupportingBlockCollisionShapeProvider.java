package net.caffeinemc.mods.lithium.common.entity.block_tracking.block_support;

import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public interface SupportingBlockCollisionShapeProvider {

    @Nullable VoxelShape lithium$getCollisionShapeBelow();
}
