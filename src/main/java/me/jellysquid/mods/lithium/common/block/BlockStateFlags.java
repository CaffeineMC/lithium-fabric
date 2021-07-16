package me.jellysquid.mods.lithium.common.block;

import me.jellysquid.mods.lithium.common.ai.pathing.PathNodeCache;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.world.chunk.ChunkSection;

public class BlockStateFlags {
    public static final boolean ENABLED = SectionFlagHolder.class.isAssignableFrom(ChunkSection.class);
    public static final int NUM_FLAGS = 2; //Update this number when adding a new flag!

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
}
