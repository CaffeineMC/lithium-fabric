package me.jellysquid.mods.lithium.common.world.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface BlockEntityGetter {
    BlockEntity lithium$getLoadedExistingBlockEntity(BlockPos pos);
}
