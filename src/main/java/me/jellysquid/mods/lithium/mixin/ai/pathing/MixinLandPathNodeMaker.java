package me.jellysquid.mods.lithium.mixin.ai.pathing;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.block.*;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.fluid.FluidState;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Determining the type of node offered by a block state is a very slow operation due to the nasty chain of tag,
 * instanceof, and block property checks. Since each blockstate can only map to one type of node, we can create a
 * cache which stores the result of this complicated code path. This provides a significant speed-up in path-finding
 * code and should be relatively safe.
 */
@SuppressWarnings("ConstantConditions")
@Mixin(LandPathNodeMaker.class)
public abstract class MixinLandPathNodeMaker {
    private static final PathNodeType[] NODE_TYPES = PathNodeType.values();

    // This is not thread-safe!
    private static final Reference2ReferenceMap<BlockState, PathNodeType> commonTypes = new Reference2ReferenceOpenHashMap<>();
    private static final Reference2ReferenceMap<BlockState, PathNodeType> neighborTypes = new Reference2ReferenceOpenHashMap<>();

    @Shadow
    private static boolean method_27138(BlockState blockState) {
        throw new UnsupportedOperationException();
    }

    /**
     * @reason Use optimized implementation
     * @author JellySquid
     */
    @Overwrite
    public static PathNodeType getCommonNodeType(BlockView blockView, BlockPos blockPos) {
        BlockState blockState = blockView.getBlockState(blockPos);

        // Check early if the block is air as it will always be open regardless of other conditions
        if (blockState.isAir()) {
            return PathNodeType.OPEN;
        }

        // Get the cached type for this block state
        PathNodeType type = commonTypes.get(blockState);

        // No result has been cached for this block state yet, so calculate and cache it
        if (type == null) {
            commonTypes.put(blockState, type = getTaggedBlockType$lithium(blockState));
        }

        // If the node type is open, it means that we were unable to determine a more specific type, so we need
        // to check the fallback path.
        if (type == PathNodeType.OPEN) {
            // This is only ever called in vanilla after all other possibilities are exhausted, but before fluid checks
            // It should be safe to perform it last in actuality and take advantage of the cache for fluid types as well
            // since fluids will always pass this check.
            if (!blockState.canPathfindThrough(blockView, blockPos, NavigationType.LAND)) {
                return PathNodeType.BLOCKED;
            }

            // All checks succeed, this path node really is open!
            return PathNodeType.OPEN;
        }

        // Return the cached value since we found an obstacle earlier
        return type;
    }

    // [VanillaCopy] LandPathNodeMaker#getCommonNodeType
    // The checks which access other world state are hoisted from this method
    private static PathNodeType getTaggedBlockType$lithium(BlockState blockState) {
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

        if (method_27138(blockState)) {
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

        if (fluid.matches(FluidTags.WATER)) {
            return PathNodeType.WATER;
        } else if (fluid.matches(FluidTags.LAVA)) {
            return PathNodeType.LAVA;
        }

        return PathNodeType.OPEN;
    }

    /**
     * @reason Use optimized implementation
     * @author JellySquid
     */
    @Overwrite
    public static PathNodeType getNodeTypeFromNeighbors(BlockView blockView, BlockPos.Mutable pos, PathNodeType type) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        for (int x2 = -1; x2 <= 1; ++x2) {
            for (int y2 = -1; y2 <= 1; ++y2) {
                for (int z2 = -1; z2 <= 1; ++z2) {
                    if (x2 != 0 || z2 != 0) {
                        pos.set(x2 + x, y2 + y, z2 + z);

                        BlockState state = blockView.getBlockState(pos);

                        // Ensure that the block isn't air first to avoid expensive map lookups
                        if (!state.isAir()) {
                            PathNodeType neighborType = getNodeTypeForNeighbor(state);

                            if (neighborType != PathNodeType.OPEN) {
                                type = neighborType;
                            }
                        }
                    }
                }
            }
        }

        return type;
    }

    private static PathNodeType getNodeTypeForNeighbor(BlockState state) {
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
        } else if (method_27138(state)) {
            type = PathNodeType.DANGER_FIRE;
        } else {
            type = PathNodeType.OPEN;
        }

        // If no obstacle is provided by this block, then use a special value to signal it
        neighborTypes.put(state, type);

        return type;
    }
}
