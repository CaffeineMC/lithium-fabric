package me.jellysquid.mods.lithium.mixin.nbt.fast_serialization;

import me.jellysquid.mods.lithium.common.nbt.NbtFastSerializer;
import me.jellysquid.mods.lithium.common.nbt.io.NbtFastReader;
import me.jellysquid.mods.lithium.common.nbt.io.NbtFastWriter;
import net.minecraft.nbt.PositionTracker;
import net.minecraft.nbt.StringTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StringTag.class)
public class MixinStringTag implements NbtFastSerializer {
    @Shadow
    private String value;

    @Override
    public void serialize(NbtFastWriter writer) {
        writer.writeString(this.value);
    }

    @Override
    public void deserialize(NbtFastReader reader, int level, PositionTracker positionTracker) {
        positionTracker.add(288L);

        this.value = reader.readString();

        positionTracker.add(16 * this.value.length());
    }
}
