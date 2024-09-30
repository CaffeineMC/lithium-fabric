package me.jellysquid.mods.lithium.common.entity.pushable;

import net.minecraft.world.level.block.state.BlockState;

public interface BlockCachingEntity {

    default void lithium$OnBlockCacheDeleted() {

    }

    default void lithium$OnBlockCacheSet(BlockState newState) {

    }

    default void lithium$SetClimbingMobCachingSectionUpdateBehavior(boolean listening) {
        throw new UnsupportedOperationException();
    }

    BlockState lithium$getCachedFeetBlockState();
}