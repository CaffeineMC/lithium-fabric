package me.jellysquid.mods.lithium.common.block;

import me.jellysquid.mods.lithium.common.ai.pathing.BlockStatePathingCache;
import me.jellysquid.mods.lithium.common.ai.pathing.PathNodeCache;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.world.chunk.ChunkSection;

import java.util.ArrayList;

public class BlockStateFlags {
    public static final boolean ENABLED = BlockCountingSection.class.isAssignableFrom(ChunkSection.class);

    public static final int NUM_LISTENING_FLAGS;
    public static final ListeningBlockStatePredicate[] LISTENING_FLAGS;

    public static final int NUM_FLAGS;
    public static final TrackedBlockStatePredicate[] FLAGS;

    public static final ListeningBlockStatePredicate FLUIDS;

    public static final TrackedBlockStatePredicate OVERSIZED_SHAPE;
    public static final TrackedBlockStatePredicate PATH_NOT_OPEN;
    public static final TrackedBlockStatePredicate WATER;
    public static final TrackedBlockStatePredicate LAVA;

    static {
        ArrayList<ListeningBlockStatePredicate> listeningFlags = new ArrayList<>();
        //noinspection ConstantConditions
        FLUIDS = new ListeningBlockStatePredicate(listeningFlags.size()) {
            @Override
            public boolean test(BlockState operand) {
                return operand.getFluidState().getFluid() != Fluids.EMPTY;
            }
        };
        listeningFlags.add(FLUIDS);
        NUM_LISTENING_FLAGS = listeningFlags.size();
        LISTENING_FLAGS = listeningFlags.toArray(new ListeningBlockStatePredicate[NUM_LISTENING_FLAGS]);


        ArrayList<TrackedBlockStatePredicate> countingFlags = new ArrayList<>(listeningFlags);

        OVERSIZED_SHAPE = new TrackedBlockStatePredicate(countingFlags.size()) {
            @Override
            public boolean test(BlockState operand) {
                return operand.exceedsCube();
            }
        };
        countingFlags.add(OVERSIZED_SHAPE);

        WATER = new TrackedBlockStatePredicate(countingFlags.size()) {
            @Override
            public boolean test(BlockState operand) {
                return operand.getFluidState().getFluid().isIn(FluidTags.WATER);
            }
        };
        countingFlags.add(WATER);

        LAVA = new TrackedBlockStatePredicate(countingFlags.size()) {
            @Override
            public boolean test(BlockState operand) {
                return operand.getFluidState().getFluid().isIn(FluidTags.LAVA);
            }
        };
        countingFlags.add(LAVA);

        if (BlockStatePathingCache.class.isAssignableFrom(AbstractBlock.AbstractBlockState.class)) {
            PATH_NOT_OPEN = new TrackedBlockStatePredicate(countingFlags.size()) {
                @Override
                public boolean test(BlockState operand) {
                    return PathNodeCache.getNeighborPathNodeType(operand) != PathNodeType.OPEN;
                }
            };
            countingFlags.add(PATH_NOT_OPEN);
        } else {
            PATH_NOT_OPEN = null;
        }

        NUM_FLAGS = countingFlags.size();
        FLAGS = countingFlags.toArray(new TrackedBlockStatePredicate[NUM_FLAGS]);
    }
}
