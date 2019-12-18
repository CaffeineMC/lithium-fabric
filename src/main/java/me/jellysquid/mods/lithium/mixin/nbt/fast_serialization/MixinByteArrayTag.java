package me.jellysquid.mods.lithium.mixin.nbt.fast_serialization;

import me.jellysquid.mods.lithium.common.nbt.NbtFastSerializer;
import me.jellysquid.mods.lithium.common.nbt.io.NbtFastReader;
import me.jellysquid.mods.lithium.common.nbt.io.NbtFastWriter;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.PositionTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ByteArrayTag.class)
public class MixinByteArrayTag implements NbtFastSerializer {
    @Shadow
    private byte[] value;

    @Override
    public void serialize(NbtFastWriter writer) {
        writer.writeInt(this.value.length);
        writer.writeByteArray(this.value);
    }

    @Override
    public void deserialize(NbtFastReader reader, int level, PositionTracker positionTracker) {
        positionTracker.add(192L);

        int size = reader.readInt();

        positionTracker.add(8 * size);

        this.value = new byte[size];

        reader.readBytes(this.value);

    }
}
