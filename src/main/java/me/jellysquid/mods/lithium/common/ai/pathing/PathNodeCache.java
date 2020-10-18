package me.jellysquid.mods.lithium.common.ai.pathing;

import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.world.chunk.ChunkSection;

public class PathNodeCache {
    /**
     * A transient hash table of chunk sections and whether or not they contain dangerous block types. Used as a cache
     * to avoid scanning for many neighbors when we know the chunk is free of dangers. This is only safe to use when
     * we know the world is not going to be modified while it is active.
     */
    private static final Reference2BooleanMap<ChunkSection> chunkNeighborDangerCache = new Reference2BooleanOpenHashMap<>();

    /**
     * True if the chunk danger cache is enabled and can be used.
     */
    private static boolean dangerCacheEnabled = false;

    /**
     * The previous chunk section that was queried for neighboring dangers.
     */
    private static ChunkSection prevQueriedNeighborSectionKey;

    /**
     * The result of the previous query belonging to {@link PathNodeCache#prevQueriedNeighborSectionKey}.
     */
    private static boolean prevQueriedNeighborSectionResult;

    /**
     * Enables the chunk danger cache. This should be called immediately before a controlled path-finding code path
     * begins so that we can accelerate nearby danger checks.
     */
    public static void enableChunkCache() {
        dangerCacheEnabled = true;
    }

    /**
     * Disables and clears the chunk danger cache. This should be called immediately before path-finding ends so that
     * block updates are reflected for future path-finding tasks.
     */
    public static void disableChunkCache() {
        dangerCacheEnabled = false;
        chunkNeighborDangerCache.clear();

        prevQueriedNeighborSectionKey = null;
        prevQueriedNeighborSectionResult = false;
    }

    private static boolean isChunkSectionDangerousNeighbor(ChunkSection section) {
        return section.getContainer()
                .hasAny(state -> getNeighborPathNodeType(state) != PathNodeType.OPEN);
    }

    public static PathNodeType getPathNodeType(BlockState state) {
        return ((BlockStatePathingCache) state).getPathNodeType();
    }

    public static PathNodeType getNeighborPathNodeType(BlockState state) {
        return ((BlockStatePathingCache) state).getNeighborPathNodeType();
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
        if (ChunkSection.isEmpty(section)) {
            return true;
        }

        // If the caching code path is disabled, the section must be assumed to potentially contain dangers
        if (!dangerCacheEnabled) {
            return false;
        }

        if (prevQueriedNeighborSectionKey != section) {
            prevQueriedNeighborSectionKey = section;
            prevQueriedNeighborSectionResult = !chunkNeighborDangerCache.computeBooleanIfAbsent(section,
                    PathNodeCache::isChunkSectionDangerousNeighbor);
        }

        return prevQueriedNeighborSectionResult;
    }
}
