package me.jellysquid.mods.lithium.mixin.nbt.fast_serialization;

import me.jellysquid.mods.lithium.common.nbt.TagSerializer;
import me.jellysquid.mods.lithium.common.nbt.io.NbtIn;
import me.jellysquid.mods.lithium.common.nbt.io.NbtOut;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.PositionTracker;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(CompoundTag.class)
public class MixinCompoundTag implements TagSerializer {
    @Shadow
    @Final
    private Map<String, Tag> tags;

    @Override
    public void serialize(NbtOut out) {
        for (String key : this.tags.keySet()) {
            Tag tag = this.tags.get(key);

            write(key, tag, out);
        }

        out.writeByte((byte) 0);
    }

    @Override
    public void deserialize(NbtIn in, int level, PositionTracker positionTracker) {
        positionTracker.add(384L);

        if (level > 512) {
            throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
        }

        this.tags.clear();

        byte type;

        while((type = in.readByte()) != 0) {
            String name = in.readString();

            positionTracker.add(224 + 16 * name.length());

            Tag tag = createTag(type, name, in, level + 1, positionTracker);

            if (this.tags.put(name, tag) != null) {
                positionTracker.add(288L);
            }
        }
    }

    private static void write(String name, Tag tag, NbtOut out) {
        out.writeByte(tag.getType());

        if (tag.getType() != 0) {
            out.writeString(name);

            ((TagSerializer) tag).serialize(out);
        }
    }

    private static Tag createTag(byte type, String key, NbtIn in, int level, PositionTracker positionTracker) {
        Tag tag = Tag.createTag(type);

        ((TagSerializer) tag).deserialize(in, level, positionTracker);

        return tag;
    }

}
