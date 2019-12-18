package me.jellysquid.mods.lithium.mixin.nbt.fast_serialization;

import me.jellysquid.mods.lithium.common.nbt.NbtFastSerializer;
import me.jellysquid.mods.lithium.common.nbt.io.NbtFastReader;
import me.jellysquid.mods.lithium.common.nbt.io.NbtFastWriter;
import net.minecraft.nbt.PositionTracker;
import net.minecraft.nbt.ShortTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ShortTag.class)
public class MixinShortTag implements NbtFastSerializer {
    @Shadow
    private short value;

    @Override
    public void serialize(NbtFastWriter writer) {
        writer.writeShort(this.value);
    }

    @Override
    public void deserialize(NbtFastReader reader, int level, PositionTracker positionTracker) {
        positionTracker.add(80L);

        this.value = reader.readShort();
    }
}
