package me.jellysquid.mods.lithium.mixin.nbt.fast_serialization;

import me.jellysquid.mods.lithium.common.nbt.TagFIO;
import me.jellysquid.mods.lithium.common.nbt.io.NbtIn;
import me.jellysquid.mods.lithium.common.nbt.io.NbtOut;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.PositionTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ByteArrayTag.class)
public class MixinByteArrayTag implements TagFIO {
    @Shadow
    private byte[] value;

    @Override
    public void serialize(NbtOut out) {
        out.writeInt(this.value.length);
        out.writeBytes(this.value);
    }

    @Override
    public void deserialize(NbtIn in, int level, PositionTracker positionTracker) {
        positionTracker.add(192L);

        int size = in.readInt();

        positionTracker.add(8 * size);

        this.value = new byte[size];

        in.readBytes(this.value);

    }
}
