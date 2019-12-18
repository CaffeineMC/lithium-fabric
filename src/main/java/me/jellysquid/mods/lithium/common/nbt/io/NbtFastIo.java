package me.jellysquid.mods.lithium.common.nbt.io;

import me.jellysquid.mods.lithium.common.nbt.NbtFastSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.PositionTracker;
import net.minecraft.nbt.Tag;

import java.io.IOException;

public class NbtFastIo {
    public static void write(CompoundTag compoundTag, NbtFastWriter writer) {
        write((Tag) compoundTag, writer);
    }

    private static void write(Tag tag, NbtFastWriter writer) {
        writer.writeByte(tag.getType());

        if (tag.getType() != 0) {
            writer.writeString("");

            ((NbtFastSerializer) tag).serialize(writer);
        }
    }

    public static CompoundTag read(NbtFastReader reader) throws IOException {
        return read(reader, PositionTracker.DEFAULT);
    }

    public static CompoundTag read(NbtFastReader reader, PositionTracker positionTracker) throws IOException {
        Tag tag = read(reader, 0, positionTracker);

        if (tag instanceof CompoundTag) {
            return (CompoundTag) tag;
        }

        throw new IOException("Root tag must be a named compound tag");
    }

    private static Tag read(NbtFastReader reader, int level, PositionTracker positionTracker) {
        byte type = reader.readByte();

        if (type == 0) {
            return new EndTag();
        }

        reader.readString();

        Tag tag = Tag.createTag(type);
        ((NbtFastSerializer) tag).deserialize(reader, level, positionTracker);

        return tag;
    }
}
