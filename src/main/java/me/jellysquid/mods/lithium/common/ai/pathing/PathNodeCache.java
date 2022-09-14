package me.jellysquid.mods.lithium.common.ai.pathing;

import me.jellysquid.mods.lithium.common.block.BlockCountingSection;
import me.jellysquid.mods.lithium.common.block.BlockStateFlags;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.ChunkSection;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

public abstract class PathNodeCache {
    private static boolean isChunkSectionDangerousNeighbor(ChunkSection section) {
        return section.getBlockStateContainer()
                .hasAny(state -> getCachedNeighborPathNodeType(state) != PathNodeType.OPEN);
    }

    @Nullable
    public static PathNodeType getCachedPathNodeType(BlockState state) {
        return ((BlockStatePathingCache) state).getCachedPathNodeType();
    }

    @Nullable
    public static PathNodeType getCachedNeighborPathNodeType(BlockState state) {
        return ((BlockStatePathingCache) state).getCachedNeighborPathNodeType();
    }

    public static PathNodeType getOrCachePathNodeType(BlockState state, BlockView world, BlockPos pos) {
        PathNodeType type = getCachedPathNodeType(state);
        if (type == null) {
            //Not cached, need to cache.
            //todo add support for fapi path node types api.
            type = Validate.notNull(PathNodeDefaults.getNodeType(state));
            //todo if the node type is dynamic, don't cache because it cannot be cached.
            ((BlockStatePathingCache) state).setCachedPathNodeType(type);
        }

        return type;
    }

    public static PathNodeType getOrCacheNeighborPathNodeType(BlockState state, BlockView world, BlockPos pos) {
        PathNodeType type = getCachedNeighborPathNodeType(state);
        if (type == null) {
            //Not cached, need to cache.
            //todo add support for fapi path node types api.
            type = Validate.notNull(PathNodeDefaults.getNeighborNodeType(state));
            //todo if the node type is dynamic, don't cache because it cannot be cached.
            ((BlockStatePathingCache) state).setCachedNeighborPathNodeType(type);
        }

        return type;
    }

    /**
     * Returns whether or not a chunk section is free of dangers. This makes use of a caching layer to greatly
     * accelerate neighbor danger checks when path-finding.
     *
     * @param section The chunk section to test for dangers
     * @return True if this neighboring section is free of any dangers, otherwise false if it could
     * potentially contain dangers
     */
    public static boolean isSectionSafeAsNeighbor(ChunkSection section) {
        // Empty sections can never contribute a danger
        if (section.isEmpty()) {
            return true;
        }

        if (BlockStateFlags.ENABLED) {
            return !((BlockCountingSection) section).anyMatch(BlockStateFlags.PATH_NOT_OPEN, true);
        }
        return !isChunkSectionDangerousNeighbor(section);
    }
}
