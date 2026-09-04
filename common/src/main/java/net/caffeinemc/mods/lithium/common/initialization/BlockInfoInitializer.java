package net.caffeinemc.mods.lithium.common.initialization;

import net.caffeinemc.mods.lithium.common.ai.pathing.BlockStatePathingCache;
import net.caffeinemc.mods.lithium.common.block.BlockStateFlagHolder;
import net.caffeinemc.mods.lithium.common.block.TrackedBlockStatePredicate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockInfoInitializer {

    // Initialize the block info.
    public static void initializeBlockInfo() {
        resetBlockInfo(); //First clear all values, since maybe recalculation uses other otherwise cached values

        if (BlockStatePathingCache.class.isAssignableFrom(BlockState.class)) {
            // Initialize the cached path node types.
            for (BlockState blockState : Block.BLOCK_STATE_REGISTRY) {
                ((BlockStatePathingCache) blockState).lithium$initializePathNodeTypeCache();
            }
        }

        if (BlockStateFlagHolder.class.isAssignableFrom(BlockState.class)) {
            // Initialize the cached block flags.
            for (BlockState blockState : Block.BLOCK_STATE_REGISTRY) {
                ((BlockStateFlagHolder) blockState).lithium$initializeFlags();
            }
        }
    }

    public static void resetBlockInfo() {
        if (BlockStatePathingCache.class.isAssignableFrom(BlockState.class)) {
            // Clear the cached path node types.
            for (BlockState blockState : Block.BLOCK_STATE_REGISTRY) {
                ((BlockStatePathingCache) blockState).lithium$clearPathTypeCache();
            }
        }

        if (BlockStateFlagHolder.class.isAssignableFrom(BlockState.class) && TrackedBlockStatePredicate.FULLY_INITIALIZED.get()) {
            // Reset the cached block flags.
            for (BlockState blockState : Block.BLOCK_STATE_REGISTRY) {
                ((BlockStateFlagHolder) blockState).lithium$resetFlags();
            }
        }
    }
}
