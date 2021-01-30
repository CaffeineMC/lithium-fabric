package me.jellysquid.mods.lithium.common.block;

import me.jellysquid.mods.lithium.common.ai.pathing.PathNodeCache;
import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.world.chunk.ChunkSection;

public class BlockStateFlags {
    public static final boolean ENABLED = FlagHolder.class.isAssignableFrom(ChunkSection.class);
    public static final int NUM_FLAGS = 2; //Update this number when adding a new flag!

    public static final Flag.CachedFlag OVERSIZED_SHAPE = new Flag.CachedFlag() {
        @Override
        public boolean test(AbstractBlock.AbstractBlockState operand) {
            return operand.exceedsCube();
        }
    };
    public static final Flag.CachedFlag PATH_NOT_OPEN = new Flag.CachedFlag() {
        @Override
        public boolean test(AbstractBlock.AbstractBlockState operand) {
            return PathNodeCache.getNeighborPathNodeType(operand) != PathNodeType.OPEN;
        }
    };
}
