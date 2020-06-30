package me.jellysquid.mods.lithium.mixin.math.fast_util;

import net.minecraft.util.math.ChunkSectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkSectionPos.class)
public class MixinChunkSectionPos {
    /**
     * @reason Better inlining & reduce bytecode instructions
     * @author Maity
     */
    @Overwrite
    public static long asLong(int x, int y, int z) {
        return (((long) x & 4194303L) << 42) | (((long) y & 1048575L)) | (((long) z & 4194303L) << 20);
    }

    @Shadow
    public static native int getSectionCoord(int i);
}
