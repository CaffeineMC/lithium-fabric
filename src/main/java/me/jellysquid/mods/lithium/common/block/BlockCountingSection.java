package me.jellysquid.mods.lithium.common.block;

import net.minecraft.block.BlockState;

public interface BlockCountingSection {
    boolean lithium$mayContainAny(TrackedBlockStatePredicate trackedBlockStatePredicate);

    void lithium$trackBlockStateChange(BlockState newState, BlockState oldState);
}
