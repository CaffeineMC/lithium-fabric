package me.jellysquid.mods.lithium.common.util.palette;

import me.jellysquid.mods.lithium.common.util.LithiumMath;
import net.minecraft.util.PackedIntegerArray;

import java.util.function.IntConsumer;

/**
 * Faster implementation of PackedIntegerArray which avoids storing integers across word boundaries at the expense of
 * some additional memory usage. This allows us to remove all the conditional logic and replace the expensive
 * modulo/divisions with fast bit operations.
 */
public class POTPackedIntegerArray extends PackedIntegerArray {
    private final long[] storage;
    private final long maxValue;

    private final int bits;
    private final int elementCount;

    private final int wordIdxShift;
    private final int bitIdxMask, bitIdxShift;

    public POTPackedIntegerArray(int bits, int count) {
        this(bits, count, new long[getArraySize(LithiumMath.nextPowerOfTwo(bits), count)]);
    }

    public POTPackedIntegerArray(int bits, int count, long[] longs) {
        // Short circuits the super call to accept our number of longs
        super(1, getArraySize(bits = LithiumMath.nextPowerOfTwo(bits), count) * 64, longs);

        this.storage = longs;
        this.maxValue = (1L << bits) - 1L;

        this.wordIdxShift = Integer.bitCount(64 / bits - 1);
        this.bitIdxShift = Integer.bitCount(bits - 1);

        this.bitIdxMask = (1 << this.wordIdxShift) - 1;
        this.elementCount = count;

        this.bits = bits;
    }

    public static int getArraySize(int bits, int count) {
        final int entriesPerWord = 64 / bits;

        return (count + entriesPerWord - 1) / entriesPerWord;
    }

    @Override
    public int setAndGetOldValue(int idx, int value) {
        final int wordIdx = idx >>> this.wordIdxShift;
        final int bitIdx = (idx & this.bitIdxMask) << this.bitIdxShift;

        final long word = this.storage[wordIdx];
        final long wordMask = ~(this.maxValue << bitIdx);

        final long otherBits = word & wordMask;
        final long thisBits = ((long) value & this.maxValue) << bitIdx;

        this.storage[wordIdx] = otherBits | thisBits;

        return (int) (word >>> bitIdx & this.maxValue);
    }

    @Override
    public void set(int idx, int value) {
        final int wordIdx = idx >>> this.wordIdxShift;
        final int bitIdx = (idx & this.bitIdxMask) << this.bitIdxShift;

        final long word = this.storage[wordIdx];
        final long wordMask = ~(this.maxValue << bitIdx);

        final long otherBits = word & wordMask;
        final long thisBits = ((long) value & this.maxValue) << bitIdx;

        this.storage[wordIdx] = otherBits | thisBits;
    }

    @Override
    public int get(int idx) {
        final int wordIdx = idx >>> this.wordIdxShift;
        final int bitIdx = (idx & this.bitIdxMask) << this.bitIdxShift;

        final long word = this.storage[wordIdx];

        return (int) (word >>> bitIdx & this.maxValue);
    }

    @Override
    public void method_21739(IntConsumer consumer) {
        // TODO: faster impl
        for (int i = 0; i < this.elementCount; i++) {
            consumer.accept(this.get(i));
        }
    }

    @Override
    public int getSize() {
        return this.elementCount;
    }

    @Override
    public int getElementBits() {
        return this.bits;
    }
}
