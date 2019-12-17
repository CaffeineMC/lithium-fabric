package me.jellysquid.mods.lithium.mixin.nbt.fast_serialization;

import me.jellysquid.mods.lithium.common.nbt.TagFIO;
import me.jellysquid.mods.lithium.common.nbt.io.NbtIn;
import me.jellysquid.mods.lithium.common.nbt.io.NbtOut;
import net.minecraft.nbt.PositionTracker;
import net.minecraft.nbt.StringTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StringTag.class)
public class MixinStringTag implements TagFIO {
    @Shadow
    private String value;

    @Override
    public void serialize(NbtOut out) {
        out.writeString(this.value);
    }

    @Override
    public void deserialize(NbtIn in, int level, PositionTracker positionTracker) {
        positionTracker.add(288L);

        this.value = in.readString();

        positionTracker.add(16 * this.value.length());
    }
}
