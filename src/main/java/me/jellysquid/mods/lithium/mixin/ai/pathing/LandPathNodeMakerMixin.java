package me.jellysquid.mods.lithium.mixin.ai.pathing;

import me.jellysquid.mods.lithium.common.ai.pathing.PathNodeCache;
import me.jellysquid.mods.lithium.common.util.Pos;
import me.jellysquid.mods.lithium.common.world.WorldHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Determining the type of node offered by a block state is a very slow operation due to the nasty chain of tag,
 * instanceof, and block property checks. Since each blockstate can only map to one type of node, we can create a
 * cache which stores the result of this complicated code path. This provides a significant speed-up in path-finding
 * code and should be relatively safe.
 */
@Mixin(LandPathNodeMaker.class)
public abstract class LandPathNodeMakerMixin {
    /**
     * @reason Use optimized implementation
     * @author JellySquid
     */
    @Overwrite
    public static PathNodeType getCommonNodeType(BlockView blockView, BlockPos blockPos) {
        BlockState blockState = blockView.getBlockState(blockPos);
        PathNodeType type = PathNodeCache.getPathNodeType(blockState);

        // If the node type is open, it means that we were unable to determine a more specific type, so we need
        // to check the fallback path.
        if (type == PathNodeType.OPEN || type == PathNodeType.WATER) {
            // This is only ever called in vanilla after all other possibilities are exhausted, but before fluid checks
            // It should be safe to perform it last in actuality and take advantage of the cache for fluid types as well
            // since fluids will always pass this check.
            if (!blockState.canPathfindThrough(blockView, blockPos, NavigationType.LAND)) {
                return PathNodeType.BLOCKED;
            }

            // All checks succeed, this path node really is open!
            return type;
        }

        // Return the cached value since we found an obstacle earlier
        return type;
    }

    /**
     * @reason Use optimized implementation which avoids scanning blocks for dangers where possible
     * @author JellySquid
     */
    @Overwrite
    public static PathNodeType getNodeTypeFromNeighbors(BlockView world, BlockPos.Mutable pos, PathNodeType type) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        ChunkSection section = null;

        // Check that all the block's neighbors are within the same chunk column. If so, we can isolate all our block
        // reads to just one chunk and avoid hits against the server chunk manager.
        if (world instanceof CollisionView && WorldHelper.areNeighborsWithinSameChunk(pos)) {
            // If the y-coordinate is within bounds, we can cache the chunk section. Otherwise, the if statement to check
            // if the cached chunk section was initialized will early-exit.
            if (!world.isOutOfHeightLimit(y)) { //todo use cached values for height limit
                // This cast is always safe and is necessary to obtain direct references to chunk sections.
                Chunk chunk = (Chunk) ((CollisionView) world).getChunkAsView(Pos.ChunkCoord.fromBlockCoord(x), Pos.ChunkCoord.fromBlockCoord(z));

                // If the chunk is absent, the cached section above will remain null, as there is no chunk section anyways.
                // An empty chunk or section will never pose any danger sources, which will be caught later.
                if (chunk != null) {
                    section = chunk.getSectionArray()[Pos.SectionYIndex.fromBlockCoord(world, y)];
                }
            }

            // If we can guarantee that blocks won't be modified while the cache is active, try to see if the chunk
            // section is empty or contains any dangerous blocks within the palette. If not, we can assume any checks
            // against this chunk section will always fail, allowing us to fast-exit.
            if (section == null || PathNodeCache.isSectionSafeAsNeighbor(section)) {
                return type;
            }
        }

        int xStart = x - 1;
        int yStart = y - 1;
        int zStart = z - 1;

        int xEnd = x + 1;
        int yEnd = y + 1;
        int zEnd = z + 1;

        // Vanilla iteration order is XYZ
        for (int adjX = xStart; adjX <= xEnd; adjX++) {
            for (int adjY = yStart; adjY <= yEnd; adjY++) {
                for (int adjZ = zStart; adjZ <= zEnd; adjZ++) {
                    // Skip the vertical column of the origin block
                    if (adjX == x && adjZ == z) {
                        continue;
                    }

                    BlockState state;

                    // If we're not accessing blocks outside a given section, we can greatly accelerate block state
                    // retrieval by calling upon the cached chunk directly.
                    if (section != null) {
                        state = section.getBlockState(adjX & 15, adjY & 15, adjZ & 15);
                    } else {
                        state = world.getBlockState(pos.set(adjX, adjY, adjZ));
                    }

                    // Ensure that the block isn't air first to avoid expensive hash table accesses
                    if (state.isAir()) {
                        continue;
                    }

                    PathNodeType neighborType = PathNodeCache.getNeighborPathNodeType(state);

                    if (neighborType != PathNodeType.OPEN) {
                        return neighborType;
                    }
                }
            }
        }

        return type;
    }
}
