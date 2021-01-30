package me.jellysquid.mods.lithium.common.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.world.chunk.ChunkSection;

public class BlockStateFlags {
    public static final boolean ENABLED = FlagHolder.class.isAssignableFrom(ChunkSection.class);
    public static final int NUM_FLAGS = 1; //Update this number when adding a new flag!

    public static final Flag.CachedFlag OVERSIZED_SHAPE = new Flag.CachedFlag() {
        @Override
        public boolean test(AbstractBlock.AbstractBlockState operand) {
            return operand.exceedsCube();
        }
    };
}
