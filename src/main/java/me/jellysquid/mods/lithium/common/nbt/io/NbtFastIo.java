package me.jellysquid.mods.lithium.common.nbt.io;

import me.jellysquid.mods.lithium.common.nbt.TagFIO;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.PositionTracker;
import net.minecraft.nbt.Tag;

import java.io.IOException;

public class NbtFastIo {
    public static void write(CompoundTag compoundTag, NbtOut out) {
        write((Tag) compoundTag, out);
    }

    private static void write(Tag tag, NbtOut out) {
        out.writeByte(tag.getType());

        if (tag.getType() != 0) {
            out.writeString("");

            ((TagFIO) tag).serialize(out);
        }
    }

    public static CompoundTag read(NbtIn in) throws IOException {
        return read(in, PositionTracker.DEFAULT);
    }

    public static CompoundTag read(NbtIn in, PositionTracker positionTracker) throws IOException {
        Tag tag_1 = read(in, 0, positionTracker);

        if (tag_1 instanceof CompoundTag) {
            return (CompoundTag)tag_1;
        } else {
            throw new IOException("Root tag must be a named compound tag");
        }
    }

    private static Tag read(NbtIn in, int level, PositionTracker positionTracker) {
        byte type = in.readByte();

        if (type == 0) {
            return new EndTag();
        }

        in.readString();

        Tag tag = Tag.createTag(type);
        ((TagFIO) tag).deserialize(in, level, positionTracker);

        return tag;
    }
}
