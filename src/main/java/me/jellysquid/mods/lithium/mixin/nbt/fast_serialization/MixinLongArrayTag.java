package me.jellysquid.mods.lithium.mixin.nbt.fast_serialization;

import me.jellysquid.mods.lithium.common.nbt.NbtFastSerializer;
import me.jellysquid.mods.lithium.common.nbt.io.NbtFastReader;
import me.jellysquid.mods.lithium.common.nbt.io.NbtFastWriter;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.PositionTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LongArrayTag.class)
public class MixinLongArrayTag implements NbtFastSerializer {

    @Shadow
    private long[] value;

    @Override
    public void serialize(NbtFastWriter writer) {
        writer.writeInt(this.value.length);
        writer.writeLongArray(this.value);
    }

    @Override
    public void deserialize(NbtFastReader reader, int level, PositionTracker positionTracker) {
        positionTracker.add(192L);
        int count = reader.readInt();

        positionTracker.add(64 * count);
        this.value = new long[count];

        for (int i = 0; i < count; ++i) {
            this.value[i] = reader.readLong();
        }
    }
}
