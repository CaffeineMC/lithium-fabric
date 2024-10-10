package net.caffeinemc.mods.lithium.common.block;

import net.minecraft.world.level.block.state.BlockState;

public interface BlockCountingSection {
    boolean lithium$mayContainAny(TrackedBlockStatePredicate trackedBlockStatePredicate);

    void lithium$trackBlockStateChange(BlockState newState, BlockState oldState);
}
