package me.jellysquid.mods.lithium.common.nbt;

import me.jellysquid.mods.lithium.common.nbt.io.NbtFastReader;
import me.jellysquid.mods.lithium.common.nbt.io.NbtFastWriter;
import net.minecraft.nbt.PositionTracker;

public interface NbtFastSerializer {
    /**
     * Serializes the {@link net.minecraft.nbt.Tag} to {@param out}.
     */
    void serialize(NbtFastWriter writer);

    /**
     * Deserializes into the {@link net.minecraft.nbt.Tag} from {@param in}.
     */
    void deserialize(NbtFastReader reader, int level, PositionTracker positionTracker);
}
