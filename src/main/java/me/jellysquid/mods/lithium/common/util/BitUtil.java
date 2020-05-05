package me.jellysquid.mods.lithium.common.util;

public class BitUtil {
    /**
     * @return True if the integer {@param value} contains a one-bit at offset {@param idx}, otherwise false
     */
    public static boolean contains(byte value, int idx) {
        return (Byte.toUnsignedInt(value) & (1 << idx)) != 0;
    }

    /**
     * Returns an integer with the bit at offset {@param idx} set to the integer representation
     * of the boolean {@param value}.
     */
    public static byte bit(int idx, boolean value) {
        return value ? flag(idx) : 0;
    }

    /**
     * Returns an integer with a single bit set at offset {@param idx}.
     */
    public static byte flag(int idx) {
        return (byte) (1 << idx);
    }
}
