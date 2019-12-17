package me.jellysquid.mods.lithium.common.nbt.region;

import me.jellysquid.mods.lithium.common.nbt.io.NbtOut;
import net.minecraft.util.math.ChunkPos;

import java.io.IOException;

public interface RegionFileDirectWritable {
    void write(ChunkPos pos, NbtOut out) throws IOException;
}
