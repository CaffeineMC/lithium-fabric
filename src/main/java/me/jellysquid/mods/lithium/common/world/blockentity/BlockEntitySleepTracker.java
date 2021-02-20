package me.jellysquid.mods.lithium.common.world.blockentity;

import net.minecraft.block.entity.BlockEntity;

public interface BlockEntitySleepTracker {
    void setAwake(BlockEntity blockEntity, boolean needsTicking);
}