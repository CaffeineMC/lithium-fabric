package me.jellysquid.mods.lithium.api.pathing;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

/**
 * Provides the ability for mods to specify what {@link PathNodeType} their block uses for path-finding. This exists
 * because Lithium replaces a large amount of entity path-finding logic, which can cause other mods which mixin to
 * this code to fail or explode into other issues.
 *
 * This interface should be added to your {@link net.minecraft.block.Block} type to replace the default implementation.
 */
public interface BlockPathingBehavior {
    /**
     * Controls how the given block state is seen in path-finding.
     *
     * If you were mixing into the method {@link LandPathNodeMaker#getCommonNodeType(BlockView, BlockPos)},
     * you will want to implement this method with your logic instead.
     *
     * The result of this method is cached in the block state and will only be called on block initialization.
     *
     * @param state The block state being examined
     * @return The path node type for the given block state
     */
    @SuppressWarnings("JavadocReference")
    PathNodeType getPathNodeType(BlockState state);

    /**
     * Controls the behavior of the "neighbor" check for path finding. This is used when scanning the blocks next
     * to another path node for nearby obstacles (i.e. dangerous blocks the entity could possibly collide with, such as
     * fire or cactus.)
     *
     * If you were mixing into the method {@link LandPathNodeMaker#getNodeTypeFromNeighbors}, you will want to implement
     * this method with your logic instead.
     *
     * The result of this method is cached in the block state and will only be called on block initialization.
     *
     * @param state The block state being examined
     * @return The path node type for the given block state when this block is being searched as a
     *         neighbor of another path node
     */
    PathNodeType getPathNodeTypeAsNeighbor(BlockState state);
}
