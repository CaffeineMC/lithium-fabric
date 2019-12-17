package me.jellysquid.mods.lithium.mixin.nbt.fast_serialization;

import me.jellysquid.mods.lithium.common.nbt.TagFIO;
import me.jellysquid.mods.lithium.common.nbt.io.NbtIn;
import me.jellysquid.mods.lithium.common.nbt.io.NbtOut;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.PositionTracker;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EndTag.class)
public class MixinEndTag implements TagFIO {
    @Override
    public void serialize(NbtOut out) {

    }

    @Override
    public void deserialize(NbtIn in, int level, PositionTracker positionTracker) {
        positionTracker.add(64L);
    }
}
