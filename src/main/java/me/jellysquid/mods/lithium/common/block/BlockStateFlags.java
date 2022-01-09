package me.jellysquid.mods.lithium.common.block;

import me.jellysquid.mods.lithium.common.ai.pathing.PathNodeCache;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.tag.FluidTags;
import net.minecraft.world.chunk.ChunkSection;

public class BlockStateFlags {
    public static final boolean ENABLED = SectionFlagHolder.class.isAssignableFrom(ChunkSection.class);
    public static final int NUM_FLAGS = 4; //Update this number when adding a new flag!

    public static final IndexedBlockStatePredicate OVERSIZED_SHAPE = new IndexedBlockStatePredicate() {
        @Override
        public boolean test(BlockState operand) {
            return operand.exceedsCube();
        }
    };
    public static final IndexedBlockStatePredicate PATH_NOT_OPEN = new IndexedBlockStatePredicate() {
        @Override
        public boolean test(BlockState operand) {
            return PathNodeCache.getNeighborPathNodeType(operand) != PathNodeType.OPEN;
        }
    };
    public static final IndexedBlockStatePredicate WATER = new IndexedBlockStatePredicate() {
        @Override
        public boolean test(BlockState operand) {
            return operand.getFluidState().getFluid().isIn(FluidTags.WATER);
        }
    };
    public static final IndexedBlockStatePredicate LAVA = new IndexedBlockStatePredicate() {
        @Override
        public boolean test(BlockState operand) {
            return operand.getFluidState().getFluid().isIn(FluidTags.LAVA);
        }
    };
    //Don't forget to update NUM_FLAGS when adding more
}
