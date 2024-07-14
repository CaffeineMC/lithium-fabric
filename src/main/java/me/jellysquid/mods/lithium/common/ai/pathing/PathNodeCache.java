package me.jellysquid.mods.lithium.common.ai.pathing;

import me.jellysquid.mods.lithium.common.block.BlockCountingSection;
import me.jellysquid.mods.lithium.common.block.BlockStateFlags;
import me.jellysquid.mods.lithium.common.util.Pos;
import me.jellysquid.mods.lithium.common.world.ChunkView;
import me.jellysquid.mods.lithium.common.world.WorldHelper;
import me.jellysquid.mods.lithium.mixin.ai.pathing.PathContextAccessor;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathContext;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;

public abstract class PathNodeCache {
    private static boolean isChunkSectionDangerousNeighbor(ChunkSection section) {
        return section.getBlockStateContainer()
                .hasAny(state -> getNeighborPathNodeType(state) != PathNodeType.OPEN);
    }

    public static PathNodeType getPathNodeType(BlockState state) {
        return ((BlockStatePathingCache) state).lithium$getPathNodeType();
    }

    public static PathNodeType getNeighborPathNodeType(AbstractBlock.AbstractBlockState state) {
        return ((BlockStatePathingCache) state).lithium$getNeighborPathNodeType();
    }

    public static boolean isSectionSafeAsNeighbor(ChunkSection section) {
        if (section.isEmpty()) {
            return true;
        }

        if (BlockStateFlags.ENABLED) {
            return !((BlockCountingSection) section).lithium$mayContainAny(BlockStateFlags.PATH_NOT_OPEN);
        }
        return !isChunkSectionDangerousNeighbor(section);
    }

    //REFACTORING
    public static PathNodeType getNodeTypeFromNeighbors(PathContext context, NodePosition pos, PathNodeType fallback) {
        BlockView world = context.getWorld();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        ChunkSection section = null;

        if (world instanceof ChunkView chunkView && WorldHelper.areNeighborsWithinSameChunkSection(x, y, z)) {
            if (!world.isOutOfHeightLimit(y)) {
                Chunk chunk = chunkView.lithium$getLoadedChunk(Pos.ChunkCoord.fromBlockCoord(x), Pos.ChunkCoord.fromBlockCoord(z));

                if (chunk != null) {
                    section = chunk.getSectionArray()[Pos.SectionYIndex.fromBlockCoord(world, y)];
                }
            }

            if (section == null || PathNodeCache.isSectionSafeAsNeighbor(section)) {
                return fallback;
            }
        }

        int xStart = x - 1;
        int yStart = y - 1;
        int zStart = z - 1;

        int xEnd = x + 1;
        int yEnd = y + 1;
        int zEnd = z + 1;

        for (int adjX = xStart; adjX <= xEnd; adjX++) {
            for (int adjY = yStart; adjY <= yEnd; adjY++) {
                for (int adjZ = zStart; adjZ <= zEnd; adjZ++) {
                    if (adjX == x && adjZ == z) {
                        continue;
                    }

                    BlockState state;

                    if (section != null) {
                        state = section.getBlockState(adjX & 15, adjY & 15, adjZ & 15);
                    } else {
                        BlockPos.Mutable posMut = ((PathContextAccessor) context).getLastNodePos().set(adjX, adjY, adjZ);
                        state = world.getBlockState(posMut);
                    }

                    if (state.isAir()) {
                        continue;
                    }

                    PathNodeType neighborType = PathNodeCache.getNeighborPathNodeType(state);

                    if (neighborType == null) {
                        neighborType = LandPathNodeMaker.getNodeTypeFromNeighbors(context, adjX + 1, adjY + 1, adjZ + 1, null);
                        if (neighborType == null) {
                            neighborType = PathNodeType.OPEN;
                        }
                    }
                    if (neighborType != PathNodeType.OPEN) {
                        return neighborType;
                    }
                }
            }
        }

        return fallback;
    }
}


