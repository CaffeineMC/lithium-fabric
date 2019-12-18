package me.jellysquid.mods.lithium.mixin.nbt.fast_serialization;

import me.jellysquid.mods.lithium.common.nbt.NbtFastSerializer;
import me.jellysquid.mods.lithium.common.nbt.io.NbtFastReader;
import me.jellysquid.mods.lithium.common.nbt.io.NbtFastWriter;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.PositionTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LongTag.class)
public class MixinLongTag implements NbtFastSerializer {
    @Shadow
    private long value;

    @Override
    public void serialize(NbtFastWriter writer) {
        writer.writeLong(this.value);
    }

    @Override
    public void deserialize(NbtFastReader reader, int level, PositionTracker positionTracker) {
        positionTracker.add(128L);

        this.value = reader.readLong();
    }
}
