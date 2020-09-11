package me.jellysquid.mods.lithium.mixin.ai.pathing;

import me.jellysquid.mods.lithium.common.ai.LandPathNodeCache;
import me.jellysquid.mods.lithium.common.world.WorldHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkCache;
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

        // Check early if the block is air as it will always be open regardless of other conditions
        if (blockState.isAir()) {
            return PathNodeType.OPEN;
        }

        PathNodeType type = LandPathNodeCache.getCachedNodeType(blockState);

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

    /**
     * @reason Use optimized implementation
     * @author JellySquid
     */
    @Overwrite
    public static PathNodeType getNodeTypeFromNeighbors(BlockView world, BlockPos.Mutable pos, PathNodeType type) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        ChunkSection section = null;

        // If the neighbors for this block are all within a single chunk section, that means all block reads will
        // come from it alone. TODO: Allow grabbing chunks from other BlockView implementations
        if (WorldHelper.areNeighborsWithinSameChunk(pos) && world instanceof ChunkCache) {
            BlockView chunk = ((ChunkCache) world).getExistingChunk(x >> 4, z >> 4);

            if (chunk != null) {
                section = ((Chunk) chunk).getSectionArray()[y >> 4];

                // If we can guarantee that blocks won't be modified while the cache is active, try to see if the chunk
                // section contains any dangerous blocks within the palette. If not, we can assume any checks against
                // this chunk section will always fail, allowing us to fast-exit.
                //
                // It's not cheap to scan the block palette initially, though it will always result in a net-gain when
                // the cache is used more than once.
                if (LandPathNodeCache.isSectionSafeAsNeighbor(section)) {
                    return type;
                }
            }
        }

        // Optimal iteration order is YZX
        for (int y2 = -1; y2 <= 1; ++y2) {
            for (int z2 = -1; z2 <= 1; ++z2) {
                for (int x2 = -1; x2 <= 1; ++x2) {
                    if (x2 == 0 && z2 == 0) {
                        continue;
                    }

                    pos.set(x2 + x, y2 + y, z2 + z);

                    BlockState state;

                    // If we're not accessing blocks outside a given section, we can greatly accelerate block state
                    // retrieval by calling upon the cached chunk directly.
                    if (section != null) {
                        state = section.getBlockState(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
                    } else {
                        state = world.getBlockState(pos);
                    }

                    // Ensure that the block isn't air first to avoid expensive hash table accesses
                    if (state.isAir()) {
                        continue;
                    }

                    PathNodeType neighborType = LandPathNodeCache.getNodeTypeForNeighbor(state);

                    if (neighborType != PathNodeType.OPEN) {
                        return neighborType;
                    }
                }
            }
        }

        return type;
    }
}
