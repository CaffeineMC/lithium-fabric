package net.caffeinemc.mods.lithium.common.ai.non_poi_block_search;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

/**
 * Uses CheckAndCacheBlockChecker to improve common block searches
 */
public class CommonBlockSearchesCheckAndCache {
    /**
     * Optimizes BlockPos::findClosestMatch
     * [Vanilla Copy] search order and chunk-loading - even though the latter is unlikely to be observable in vanilla.
     */
    public static boolean hasNoMatch(LevelReader levelReader, LivingEntity livingEntity,
                                     int horizontalRange, int verticalRange,
                                     Predicate<BlockState> blockStatePredicate,
                                     boolean shouldChunkLoad) {
        BlockPos mobPos = livingEntity.blockPosition();
        CheckAndCacheBlockChecker checker = new CheckAndCacheBlockChecker(
                mobPos, horizontalRange, verticalRange, levelReader, blockStatePredicate, shouldChunkLoad);
        checker.initializeChunks();
        return checker.shouldStop();
    }
}
