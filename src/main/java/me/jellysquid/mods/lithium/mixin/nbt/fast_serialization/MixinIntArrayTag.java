package me.jellysquid.mods.lithium.mixin.nbt.fast_serialization;

import me.jellysquid.mods.lithium.common.nbt.NbtFastSerializer;
import me.jellysquid.mods.lithium.common.nbt.io.NbtFastReader;
import me.jellysquid.mods.lithium.common.nbt.io.NbtFastWriter;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.PositionTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(IntArrayTag.class)
public class MixinIntArrayTag implements NbtFastSerializer {
    @Shadow
    private int[] value;

    @Override
    public void serialize(NbtFastWriter writer) {
        writer.writeInt(this.value.length);

        for (int i : this.value) {
            writer.writeInt(i);
        }
    }

    @Override
    public void deserialize(NbtFastReader reader, int level, PositionTracker positionTracker) {
        positionTracker.add(192L);

        int size = reader.readInt();

        positionTracker.add(32 * size);

        this.value = new int[size];

        for(int i = 0; i < size; ++i) {
            this.value[i] = reader.readInt();
        }
    }
}
