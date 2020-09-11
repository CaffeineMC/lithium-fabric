package me.jellysquid.mods.lithium.common.ai;

import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.block.*;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.fluid.FluidState;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.FluidTags;
import net.minecraft.world.chunk.ChunkSection;

public class LandPathNodeCache {
    private static final Reference2ReferenceMap<BlockState, PathNodeType> commonTypes = new Reference2ReferenceOpenHashMap<>();
    private static final Reference2ReferenceMap<BlockState, PathNodeType> neighborTypes = new Reference2ReferenceOpenHashMap<>();

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
     * The result of the previous query belonging to {@link LandPathNodeCache#prevQueriedNeighborSectionKey}.
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

    // [VanillaCopy] LandPathNodeMaker#getCommonNodeType
    // The checks which access other world state are hoisted from this method
    private static PathNodeType getTaggedBlockType(BlockState blockState) {
        Block block = blockState.getBlock();
        Material material = blockState.getMaterial();

        if (blockState.isIn(BlockTags.TRAPDOORS) || blockState.isOf(Blocks.LILY_PAD)) {
            return PathNodeType.TRAPDOOR;
        }

        if (blockState.isOf(Blocks.CACTUS)) {
            return PathNodeType.DAMAGE_CACTUS;
        }

        if (blockState.isOf(Blocks.SWEET_BERRY_BUSH)) {
            return PathNodeType.DAMAGE_OTHER;
        }

        if (blockState.isOf(Blocks.HONEY_BLOCK)) {
            return PathNodeType.STICKY_HONEY;
        }

        if (blockState.isOf(Blocks.COCOA)) {
            return PathNodeType.COCOA;
        }

        if (isFireDangerSource(blockState)) {
            return PathNodeType.DAMAGE_FIRE;
        }

        if (DoorBlock.isWoodenDoor(blockState) && !blockState.get(DoorBlock.OPEN)) {
            return PathNodeType.DOOR_WOOD_CLOSED;
        }

        if ((block instanceof DoorBlock) && (material == Material.METAL) && !blockState.get(DoorBlock.OPEN)) {
            return PathNodeType.DOOR_IRON_CLOSED;
        }

        if ((block instanceof DoorBlock) && blockState.get(DoorBlock.OPEN)) {
            return PathNodeType.DOOR_OPEN;
        }

        if (block instanceof AbstractRailBlock) {
            return PathNodeType.RAIL;
        }

        if (block instanceof LeavesBlock) {
            return PathNodeType.LEAVES;
        }

        if (block.isIn(BlockTags.FENCES) || block.isIn(BlockTags.WALLS) || ((block instanceof FenceGateBlock) && !blockState.get(FenceGateBlock.OPEN))) {
            return PathNodeType.FENCE;
        }

        // Retrieve the fluid state from the block state to avoid a second lookup
        FluidState fluid = blockState.getFluidState();

        if (fluid.isIn(FluidTags.WATER)) {
            return PathNodeType.WATER;
        } else if (fluid.isIn(FluidTags.LAVA)) {
            return PathNodeType.LAVA;
        }

        return PathNodeType.OPEN;
    }

    public static PathNodeType getNodeTypeForNeighbor(BlockState state) {
        PathNodeType type = neighborTypes.get(state);

        // We already cached a type for this block state, so return it
        if (type != null) {
            return type;
        }

        // [VanillaCopy] LandPathNodeMaker#getNodeTypeFromNeighbors
        // Determine what kind of obstacle type this neighbor is
        if (state.isOf(Blocks.CACTUS)) {
            type = PathNodeType.DANGER_CACTUS;
        } else if (state.isOf(Blocks.SWEET_BERRY_BUSH)) {
            type = PathNodeType.DANGER_OTHER;
        } else if (isFireDangerSource(state)) {
            type = PathNodeType.DANGER_FIRE;
        } else if (state.getFluidState().isIn(FluidTags.WATER)) {
            return PathNodeType.WATER_BORDER;
        } else {
            type = PathNodeType.OPEN;
        }

        // If no obstacle is provided by this block, then use a special value to signal it
        neighborTypes.put(state, type);

        return type;
    }

    private static boolean isFireDangerSource(BlockState blockState) {
        return blockState.isIn(BlockTags.FIRE) || blockState.isOf(Blocks.LAVA) || blockState.isOf(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(blockState);
    }

    private static boolean isChunkSectionDangerousNeighbor(ChunkSection section) {
        return section.getContainer()
                .hasAny(LandPathNodeCache::isNeighborDangerous);
    }

    private static boolean isNeighborDangerous(BlockState state) {
        return getNodeTypeForNeighbor(state) != PathNodeType.OPEN;
    }

    public static PathNodeType getCachedNodeType(BlockState blockState) {
        // Get the cached type for this block state
        PathNodeType type = commonTypes.get(blockState);

        // No result has been cached for this block state yet, so calculate and cache it
        if (type == null) {
            commonTypes.put(blockState, type = LandPathNodeCache.getTaggedBlockType(blockState));
        }

        return type;
    }

    /**
     * Returns whether or not a chunk section is free of dangers. This makes use of a caching layer to greatly
     * accelerate neighbor danger checks when path-finding.
     *
     * @param section The chunk section to test for dangers
     * @return True if this neighboring section is free of any dangers, otherwise false if it could
     *         potentially contain dangers
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
                    LandPathNodeCache::isChunkSectionDangerousNeighbor);
        }

        return prevQueriedNeighborSectionResult;
    }

}
