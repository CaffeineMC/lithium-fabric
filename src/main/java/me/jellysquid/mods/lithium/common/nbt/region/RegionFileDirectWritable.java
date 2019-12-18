package me.jellysquid.mods.lithium.common.nbt.region;

import net.minecraft.util.math.ChunkPos;

import java.io.IOException;

public interface RegionFileDirectWritable {
    void write(ChunkPos pos, byte[] bytes) throws IOException;
}
