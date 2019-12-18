package me.jellysquid.mods.lithium.mixin.nbt.fast_serialization;

import me.jellysquid.mods.lithium.common.nbt.NbtFastSerializer;
import me.jellysquid.mods.lithium.common.nbt.io.NbtFastReader;
import me.jellysquid.mods.lithium.common.nbt.io.NbtFastWriter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.PositionTracker;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(CompoundTag.class)
public class MixinCompoundTag implements NbtFastSerializer {
    @Shadow
    @Final
    private Map<String, Tag> tags;

    @Override
    public void serialize(NbtFastWriter writer) {
        for (String key : this.tags.keySet()) {
            Tag tag = this.tags.get(key);

            write(key, tag, writer);
        }

        writer.writeByte((byte) 0);
    }

    @Override
    public void deserialize(NbtFastReader reader, int level, PositionTracker positionTracker) {
        positionTracker.add(384L);

        if (level > 512) {
            throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
        }

        this.tags.clear();

        byte type;

        while((type = reader.readByte()) != 0) {
            String name = reader.readString();

            positionTracker.add(224 + 16 * name.length());

            Tag tag = createTag(type, name, reader, level + 1, positionTracker);

            if (this.tags.put(name, tag) != null) {
                positionTracker.add(288L);
            }
        }
    }

    private static void write(String name, Tag tag, NbtFastWriter out) {
        out.writeByte(tag.getType());

        if (tag.getType() != 0) {
            out.writeString(name);

            ((NbtFastSerializer) tag).serialize(out);
        }
    }

    private static Tag createTag(byte type, String key, NbtFastReader in, int level, PositionTracker positionTracker) {
        Tag tag = Tag.createTag(type);

        ((NbtFastSerializer) tag).deserialize(in, level, positionTracker);

        return tag;
    }

}
