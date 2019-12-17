package me.jellysquid.mods.lithium.mixin.nbt.fast_serialization;

import me.jellysquid.mods.lithium.common.nbt.TagFIO;
import me.jellysquid.mods.lithium.common.nbt.io.NbtIn;
import me.jellysquid.mods.lithium.common.nbt.io.NbtOut;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.PositionTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FloatTag.class)
public class MixinFloatTag implements TagFIO {
    @Shadow
    private float value;

    @Override
    public void serialize(NbtOut out) {
        out.writeFloat(this.value);
    }

    @Override
    public void deserialize(NbtIn in, int level, PositionTracker positionTracker) {
        positionTracker.add(96L);

        this.value = in.readFloat();
    }
}
