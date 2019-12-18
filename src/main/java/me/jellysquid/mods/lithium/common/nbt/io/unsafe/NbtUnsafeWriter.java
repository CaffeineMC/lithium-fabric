package me.jellysquid.mods.lithium.common.nbt.io.unsafe;

import me.jellysquid.mods.lithium.common.nbt.io.NbtFastWriter;
import me.jellysquid.mods.lithium.common.util.unsafe.UnsafeUtil;
import sun.misc.Unsafe;

import java.nio.BufferOverflowException;
import java.nio.ByteOrder;

public class NbtUnsafeWriter implements NbtFastWriter, AutoCloseable {
    private static final Unsafe UNSAFE = UnsafeUtil.getInstance();

    private static final boolean SWAP_BITS = ByteOrder.nativeOrder() != ByteOrder.BIG_ENDIAN;

    private static final long BYTE_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);

    private long memBase, memSize;

    private long writeIdx;

    public NbtUnsafeWriter(long initialSize) {
        this.memBase = UNSAFE.allocateMemory(initialSize);
        this.memSize = initialSize;
    }

    public void ensureCapacity(long size) {
        if (this.memSize - this.writeIdx <= size) {
            this.memSize = Math.max(this.memSize + size, this.memSize * 2);
            this.memBase = UNSAFE.reallocateMemory(this.memBase, this.memSize);
        }
    }

    @Override
    public void writeByte(byte value) {
        this.ensureCapacity(1);

        UNSAFE.putByte(null, this.memBase + this.writeIdx, value);
        this.writeIdx += 1;
    }

    @Override
    public void writeByteArray(byte[] bytes, int offset, int count) {
        this.ensureCapacity(count);

        UNSAFE.copyMemory(bytes, BYTE_ARRAY_OFFSET + offset, null, this.memBase + this.writeIdx, count);
        this.writeIdx += count;
    }

    @Override
    public void writeShort(short value) {
        this.ensureCapacity(2);

        UNSAFE.putShort(null, this.memBase + this.writeIdx, SWAP_BITS ? Short.reverseBytes(value) : value);
        this.writeIdx += 2;
    }

    @Override
    public void writeInt(int value) {
        this.ensureCapacity(4);

        UNSAFE.putInt(null, this.memBase + this.writeIdx, SWAP_BITS ? Integer.reverseBytes(value) : value);
        this.writeIdx += 4;
    }

    @Override
    public void writeLong(long value) {
        this.ensureCapacity(8);

        UNSAFE.putLong(null, this.memBase + this.writeIdx, SWAP_BITS ? Long.reverseBytes(value) : value);
        this.writeIdx += 8;
    }

    @Override
    public void writeLongArray(long[] array) {
        this.ensureCapacity(array.length * 8);

        for (long value : array) {
            UNSAFE.putLong(null, this.memBase + this.writeIdx, SWAP_BITS ? Long.reverseBytes(value) : value);
            this.writeIdx += 8;
        }
    }

    @Override
    public void writeString(String str) {
        final int strLen = str.length();
        int utfLen = strLen; // optimized for ASCII

        for (int i = 0; i < strLen; i++) {
            int c = str.charAt(i);

            if (c >= 0x80 || c == 0) {
                utfLen += (c >= 0x800) ? 2 : 1;
            }
        }

        if (utfLen > 65535 || /* overflow */ utfLen < strLen) {
            throw new IllegalArgumentException("String too large");
        }

        this.ensureCapacity(utfLen + 2);

        long pointer = this.memBase + this.writeIdx;

        UNSAFE.putByte(null, pointer++, (byte) ((utfLen >>> 8) & 0xFF));
        UNSAFE.putByte(null, pointer++, (byte) ((utfLen) & 0xFF));

        int i = 0;

        // optimized for initial run of ASCII
        while (i < strLen) {
            int c = str.charAt(i);

            // hit non-ASCII character, fallback!
            if (c >= 0x80 || c == 0) {
                break;
            }

            UNSAFE.putByte(null, pointer++, (byte) c);

            i++;
        }

        // now encoding non-ASCII
        while (i < strLen) {
            int c = str.charAt(i);

            if (c < 0x80 && c != 0) {
                UNSAFE.putByte(null, pointer++, (byte) c);
            } else if (c >= 0x800) {
                UNSAFE.putByte(null, pointer++, (byte) (0xE0 | ((c >> 12) & 0x0F)));
                UNSAFE.putByte(null, pointer++, (byte) (0x80 | ((c >> 6) & 0x3F)));
                UNSAFE.putByte(null, pointer++, (byte) (0x80 | ((c) & 0x3F)));
            } else {
                UNSAFE.putByte(null, pointer++, (byte) (0xC0 | ((c >> 6) & 0x1F)));
                UNSAFE.putByte(null, pointer++, (byte) (0x80 | ((c) & 0x3F)));
            }

            i++;
        }

        this.writeIdx = pointer - this.memBase;
    }

    @Override
    public byte[] finish() {
        long len = this.writeIdx;

        if (len > Integer.MAX_VALUE) {
            throw new BufferOverflowException();
        }

        byte[] copy = new byte[(int) len];

        UNSAFE.copyMemory(null, this.memBase, copy, BYTE_ARRAY_OFFSET, len);

        this.writeIdx = 0;

        return copy;
    }

    @Override
    public void close() {
        UNSAFE.freeMemory(this.memBase);
    }
}
