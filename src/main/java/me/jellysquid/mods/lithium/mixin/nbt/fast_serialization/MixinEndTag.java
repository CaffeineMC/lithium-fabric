package me.jellysquid.mods.lithium.mixin.nbt.fast_serialization;

import me.jellysquid.mods.lithium.common.nbt.NbtFastSerializer;
import me.jellysquid.mods.lithium.common.nbt.io.NbtFastReader;
import me.jellysquid.mods.lithium.common.nbt.io.NbtFastWriter;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.PositionTracker;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EndTag.class)
public class MixinEndTag implements NbtFastSerializer {
    @Override
    public void serialize(NbtFastWriter writer) {

    }

    @Override
    public void deserialize(NbtFastReader reader, int level, PositionTracker positionTracker) {
        positionTracker.add(64L);
    }
}
