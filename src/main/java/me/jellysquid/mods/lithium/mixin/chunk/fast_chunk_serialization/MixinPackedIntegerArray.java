package me.jellysquid.mods.lithium.mixin.chunk.fast_chunk_serialization;

import me.jellysquid.mods.lithium.common.world.chunk.CompactingPackedIntegerArray;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.world.chunk.Palette;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Extends {@link PackedIntegerArray} with a special compaction method defined in {@link CompactingPackedIntegerArray}.
 */
@Mixin(PackedIntegerArray.class)
public class MixinPackedIntegerArray implements CompactingPackedIntegerArray {
    @Shadow
    @Final
    private long[] storage;

    @Shadow
    @Final
    private int size;

    @Shadow
    @Final
    private int elementBits;

    @Shadow
    @Final
    private long maxValue;

    @Override
    public <T> short[] compact(Palette<T> srcPalette, Palette<T> destPalette, T def) {
        if (this.size >= Short.MAX_VALUE) {
            throw new IllegalStateException("Array too large");
        }

        short[] flattened = new short[this.size];
        short[] unique = new short[this.size];

        int len = this.storage.length;

        if (len == 0) {
            return flattened;
        }

        int prevWord = 0;

        long word = this.storage[0];
        long nextWord = (len > 1) ? this.storage[1] : 0L;

        int bits = 0;
        int i = 0;

        while (i < this.size) {
            int wordIdx = bits >> 6;
            int nextWordIdx = ((bits + this.elementBits) - 1) >> 6;
            int bitIdx = bits ^ (wordIdx << 6);

            if (wordIdx != prevWord) {
                word = nextWord;
                nextWord = ((wordIdx + 1) < len) ? this.storage[wordIdx + 1] : 0L;
                prevWord = wordIdx;
            }

            int j;

            if (wordIdx == nextWordIdx) {
                j = (int) ((word >>> bitIdx) & this.maxValue);
            } else {
                j = (int) (((word >>> bitIdx) | (nextWord << (64 - bitIdx))) & this.maxValue);
            }

            if (j != 0) {
                int remappedPalettedId = unique[j];

                if (remappedPalettedId == 0) {
                    T obj = srcPalette.getByIndex(j);
                    int id = destPalette.getIndex(obj);

                    remappedPalettedId = id + 1;

                    unique[j] = (short) remappedPalettedId;
                }

                flattened[i] = (short) (remappedPalettedId - 1);
            }

            bits += this.elementBits;
            ++i;
        }

        return flattened;
    }
}
