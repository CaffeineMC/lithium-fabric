package me.jellysquid.mods.lithium.common.nbt;

import me.jellysquid.mods.lithium.common.nbt.io.NbtIn;
import me.jellysquid.mods.lithium.common.nbt.io.NbtOut;
import net.minecraft.nbt.PositionTracker;

public interface TagSerializer {
    /**
     * Serializes the {@link net.minecraft.nbt.Tag} to {@param out}.
     */
    void serialize(NbtOut out);

    /**
     * Deserializes into the {@link net.minecraft.nbt.Tag} from {@param in}.
     */
    void deserialize(NbtIn in, int level, PositionTracker positionTracker);
}
