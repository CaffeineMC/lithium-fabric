package me.jellysquid.mods.lithium.mixin.chunk.serialization;

import me.jellysquid.mods.lithium.common.world.chunk.CompactingPackedIntegerArray;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.world.chunk.Palette;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Arrays;

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
    public <T> void compact(Palette<T> srcPalette, Palette<T> dstPalette, short[] out) {
        if (this.size >= Short.MAX_VALUE) {
            throw new IllegalStateException("Array too large");
        }

        if (this.size != out.length) {
            throw new IllegalStateException("Array size mismatch");
        }

        short[] mappings = new short[(int) (this.maxValue + 1)];

        long[] storage = this.storage;
        int size = this.size;
        int elementBits = this.elementBits;

        int arrayLen = storage.length;
        int prevWordIdx = 0;

        long word = storage[0];
        long nextWord = (arrayLen > 1) ? storage[1] : 0L;

        int bits = 0;
        int idx = 0;

        Arrays.fill(out, (short) 0);

        while (idx < size) {
            int wordIdx = bits >> 6;
            int nextWordIdx = ((bits + elementBits) - 1) >> 6;

            int bitIdx = bits ^ (wordIdx << 6);

            if (wordIdx != prevWordIdx) {
                word = nextWord;

                if ((wordIdx + 1) < arrayLen) {
                    nextWord = storage[wordIdx + 1];
                } else {
                    nextWord = 0L;
                }

                prevWordIdx = wordIdx;
            }

            int value;

            if (wordIdx == nextWordIdx) {
                value = (int) ((word >>> bitIdx) & this.maxValue);
            } else {
                value = (int) (((word >>> bitIdx) | (nextWord << (64 - bitIdx))) & this.maxValue);
            }

            int remappedId = mappings[value];

            if (remappedId == 0) {
                remappedId = dstPalette.getIndex(srcPalette.getByIndex(value)) + 1;

                mappings[value] = (short) remappedId;
            }

            out[idx] = (short) (remappedId - 1);

            bits += elementBits;
            idx++;
        }
    }
}
