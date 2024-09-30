package me.jellysquid.mods.lithium.common.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ChunkRandomSource {
    /**
     * Alternative implementation of {@link Level#getBlockRandomPos(int, int, int, int)} which does not allocate
     * a new {@link BlockPos}.
     */
    void lithium$getRandomPosInChunk(int x, int y, int z, int mask, BlockPos.MutableBlockPos out);
}
