package me.jellysquid.mods.lithium.mixin.nbt.fast_serialization;

import me.jellysquid.mods.lithium.common.nbt.TagFIO;
import me.jellysquid.mods.lithium.common.nbt.io.NbtIn;
import me.jellysquid.mods.lithium.common.nbt.io.NbtOut;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.PositionTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LongArrayTag.class)
public class MixinLongArrayTag implements TagFIO {

    @Shadow
    private long[] value;

    @Override
    public void serialize(NbtOut out) {
        out.writeInt(this.value.length);

        for (long l : this.value) {
            out.writeLong(l);
        }
    }

    @Override
    public void deserialize(NbtIn in, int level, PositionTracker positionTracker) {
        positionTracker.add(192L);
        int count = in.readInt();

        positionTracker.add(64 * count);
        this.value = new long[count];

        for (int i = 0; i < count; ++i) {
            this.value[i] = in.readLong();
        }
    }
}
