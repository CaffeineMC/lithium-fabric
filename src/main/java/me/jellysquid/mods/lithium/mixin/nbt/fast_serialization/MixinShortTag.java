package me.jellysquid.mods.lithium.mixin.nbt.fast_serialization;

import me.jellysquid.mods.lithium.common.nbt.TagSerializer;
import me.jellysquid.mods.lithium.common.nbt.io.NbtIn;
import me.jellysquid.mods.lithium.common.nbt.io.NbtOut;
import net.minecraft.nbt.PositionTracker;
import net.minecraft.nbt.ShortTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ShortTag.class)
public class MixinShortTag implements TagSerializer {
    @Shadow
    private short value;

    @Override
    public void serialize(NbtOut out) {
        out.writeShort(this.value);
    }

    @Override
    public void deserialize(NbtIn in, int level, PositionTracker positionTracker) {
        positionTracker.add(80L);

        this.value = in.readShort();
    }
}
