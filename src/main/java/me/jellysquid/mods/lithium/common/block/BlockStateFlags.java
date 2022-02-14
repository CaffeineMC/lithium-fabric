package me.jellysquid.mods.lithium.common.block;

import me.jellysquid.mods.lithium.common.ai.pathing.BlockStatePathingCache;
import me.jellysquid.mods.lithium.common.ai.pathing.PathNodeCache;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.tag.FluidTags;
import net.minecraft.world.chunk.ChunkSection;

import java.util.ArrayList;

public class BlockStateFlags {
    public static final boolean ENABLED = BlockCountingSection.class.isAssignableFrom(ChunkSection.class);
    public static final int NUM_FLAGS;

    public static final TrackedBlockStatePredicate OVERSIZED_SHAPE;
    public static final TrackedBlockStatePredicate PATH_NOT_OPEN;
    public static final TrackedBlockStatePredicate WATER;
    public static final TrackedBlockStatePredicate LAVA;
    public static final TrackedBlockStatePredicate[] ALL_FLAGS;

    static {
        ArrayList<TrackedBlockStatePredicate> allFlags = new ArrayList<>();

        //noinspection ConstantConditions
        OVERSIZED_SHAPE = new TrackedBlockStatePredicate(allFlags.size()) {
            @Override
            public boolean test(BlockState operand) {
                return operand.exceedsCube();
            }
        };
        allFlags.add(OVERSIZED_SHAPE);

        WATER = new TrackedBlockStatePredicate(allFlags.size()) {
            @Override
            public boolean test(BlockState operand) {
                return operand.getFluidState().getFluid().isIn(FluidTags.WATER);
            }
        };
        allFlags.add(WATER);

        LAVA = new TrackedBlockStatePredicate(allFlags.size()) {
            @Override
            public boolean test(BlockState operand) {
                return operand.getFluidState().getFluid().isIn(FluidTags.LAVA);
            }
        };
        allFlags.add(LAVA);

        if (BlockStatePathingCache.class.isAssignableFrom(AbstractBlock.AbstractBlockState.class)) {
            PATH_NOT_OPEN = new TrackedBlockStatePredicate(allFlags.size()) {
                @Override
                public boolean test(BlockState operand) {
                    return PathNodeCache.getNeighborPathNodeType(operand) != PathNodeType.OPEN;
                }
            };
            allFlags.add(PATH_NOT_OPEN);
        } else {
            PATH_NOT_OPEN = null;
        }

        NUM_FLAGS = allFlags.size();
        ALL_FLAGS = allFlags.toArray(new TrackedBlockStatePredicate[NUM_FLAGS]);
    }
}
