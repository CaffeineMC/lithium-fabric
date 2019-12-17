package me.jellysquid.mods.lithium.mixin.nbt.fast_serialization;

import me.jellysquid.mods.lithium.common.nbt.TagFIO;
import me.jellysquid.mods.lithium.common.nbt.io.NbtIn;
import me.jellysquid.mods.lithium.common.nbt.io.NbtOut;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.PositionTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ByteTag.class)
public class MixinByteTag implements TagFIO {
    @Shadow
    private byte value;

    @Override
    public void serialize(NbtOut out) {
        out.writeByte(this.value);
    }

    @Override
    public void deserialize(NbtIn in, int level, PositionTracker positionTracker) {
        positionTracker.add(72L);

        this.value = in.readByte();
    }
}
