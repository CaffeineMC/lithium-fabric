package me.jellysquid.mods.lithium.common.nbt;

import me.jellysquid.mods.lithium.common.nbt.io.NbtIn;
import me.jellysquid.mods.lithium.common.nbt.io.NbtOut;
import net.minecraft.nbt.PositionTracker;

public interface TagFIO {
    void serialize(NbtOut out);

    void deserialize(NbtIn in, int level, PositionTracker positionTracker);
}
