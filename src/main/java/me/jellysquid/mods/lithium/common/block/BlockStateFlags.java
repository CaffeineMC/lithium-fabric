package me.jellysquid.mods.lithium.common.block;

import me.jellysquid.mods.lithium.common.ai.pathing.PathNodeCache;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.tag.FluidTags;
import net.minecraft.world.chunk.ChunkSection;

public class BlockStateFlags {
    public static final boolean ENABLED = BlockCountingSection.class.isAssignableFrom(ChunkSection.class);
    public static final int NUM_FLAGS = 4; //Update this number when adding a new flag!

    public static final TrackedBlockStatePredicate OVERSIZED_SHAPE = new TrackedBlockStatePredicate() {
        @Override
        public boolean test(BlockState operand) {
            return operand.exceedsCube();
        }
    };
    public static final TrackedBlockStatePredicate PATH_NOT_OPEN = new TrackedBlockStatePredicate() {
        @Override
        public boolean test(BlockState operand) {
            return PathNodeCache.getNeighborPathNodeType(operand) != PathNodeType.OPEN;
        }
    };
    public static final TrackedBlockStatePredicate WATER = new TrackedBlockStatePredicate() {
        @Override
        public boolean test(BlockState operand) {
            return operand.getFluidState().getFluid().isIn(FluidTags.WATER);
        }
    };
    public static final TrackedBlockStatePredicate LAVA = new TrackedBlockStatePredicate() {
        @Override
        public boolean test(BlockState operand) {
            return operand.getFluidState().getFluid().isIn(FluidTags.LAVA);
        }
    };
    //Don't forget to update NUM_FLAGS when adding more
}
