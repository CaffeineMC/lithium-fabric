package me.jellysquid.mods.lithium.common.nbt;

import me.jellysquid.mods.lithium.common.nbt.io.NbtIn;
import me.jellysquid.mods.lithium.common.nbt.io.NbtOut;
import net.minecraft.nbt.PositionTracker;
import net.minecraft.nbt.Tag;

public class TagFIOHelper {
    public static void write(String name, Tag tag, NbtOut out) {
        out.writeByte(tag.getType());

        if (tag.getType() != 0) {
            out.writeString(name);

            ((TagFIO) tag).serialize(out);
        }
    }

    public static Tag createTag(byte type, String key, NbtIn in, int level, PositionTracker positionTracker) {
        Tag tag = Tag.createTag(type);

        ((TagFIO) tag).deserialize(in, level, positionTracker);

        return tag;
    }
}
