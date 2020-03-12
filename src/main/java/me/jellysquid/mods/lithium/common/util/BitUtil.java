package me.jellysquid.mods.lithium.common.util;

public class BitUtil {
    public static boolean contains(byte value, int idx) {
        return (Byte.toUnsignedInt(value) & (1 << idx)) != 0;
    }

    public static byte bit(int idx, boolean value) {
        return value ? flag(idx) : 0;
    }

    public static byte flag(int idx) {
        return (byte) (1 << idx);
    }
}
