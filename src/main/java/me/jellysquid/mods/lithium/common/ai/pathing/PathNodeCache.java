package me.jellysquid.mods.lithium.common.ai.pathing;

import me.jellysquid.mods.lithium.common.block.BlockStateFlags;
import me.jellysquid.mods.lithium.common.block.SectionFlagHolder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.world.chunk.ChunkSection;

public abstract class PathNodeCache {
    private static boolean isChunkSectionDangerousNeighbor(ChunkSection section) {
        return section.getContainer()
                .hasAny(state -> getNeighborPathNodeType(state) != PathNodeType.OPEN);
    }

    public static PathNodeType getPathNodeType(BlockState state) {
        return ((BlockStatePathingCache) state).getPathNodeType();
    }

    public static PathNodeType getNeighborPathNodeType(AbstractBlock.AbstractBlockState state) {
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

        if (BlockStateFlags.ENABLED) {
            return !((SectionFlagHolder) section).getFlag(BlockStateFlags.PATH_NOT_OPEN);
        }
        return !isChunkSectionDangerousNeighbor(section);
    }
}
