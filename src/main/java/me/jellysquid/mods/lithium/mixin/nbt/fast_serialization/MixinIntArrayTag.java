package me.jellysquid.mods.lithium.mixin.nbt.fast_serialization;

import me.jellysquid.mods.lithium.common.nbt.TagSerializer;
import me.jellysquid.mods.lithium.common.nbt.io.NbtIn;
import me.jellysquid.mods.lithium.common.nbt.io.NbtOut;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.PositionTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(IntArrayTag.class)
public class MixinIntArrayTag implements TagSerializer {
    @Shadow
    private int[] value;

    @Override
    public void serialize(NbtOut out) {
        out.writeInt(this.value.length);

        for (int i : this.value) {
            out.writeInt(i);
        }
    }

    @Override
    public void deserialize(NbtIn in, int level, PositionTracker positionTracker) {
        positionTracker.add(192L);

        int size = in.readInt();

        positionTracker.add(32 * size);

        this.value = new int[size];

        for(int i = 0; i < size; ++i) {
            this.value[i] = in.readInt();
        }
    }
}
