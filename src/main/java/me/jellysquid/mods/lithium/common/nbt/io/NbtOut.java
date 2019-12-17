package me.jellysquid.mods.lithium.common.nbt.io;

import java.nio.ByteBuffer;

public interface NbtOut {
    void writeInt(int value);

    void writeBytes(byte[] value);

    void writeBytes(byte[] bytes, int offset, int count);

    void writeByte(byte value);

    void writeShort(short value);

    void writeDouble(double value);

    void writeFloat(float value);

    void writeLong(long value);

    default void writeString(String str) {
        final int strlen = str.length();
        int utflen = strlen; // optimized for ASCII

        for (int i = 0; i < strlen; i++) {
            int c = str.charAt(i);
            if (c >= 0x80 || c == 0) {
                utflen += (c >= 0x800) ? 2 : 1;
            }
        }

        if (utflen > 65535 || /* overflow */ utflen < strlen) {
            throw new ArrayIndexOutOfBoundsException();
        }

        int count = 0;

        final byte[] bytearr = new byte[utflen + 2];
        bytearr[count++] = (byte) ((utflen >>> 8) & 0xFF);
        bytearr[count++] = (byte) ((utflen >>> 0) & 0xFF);

        int i;

        for (i = 0; i < strlen; i++) { // optimized for initial run of ASCII
            int c = str.charAt(i);
            if (c >= 0x80 || c == 0) {
                break;
            }
            bytearr[count++] = (byte) c;
        }

        for (; i < strlen; i++) {
            int c = str.charAt(i);

            if (c < 0x80 && c != 0) {
                bytearr[count++] = (byte) c;
            } else if (c >= 0x800) {
                bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytearr[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
                bytearr[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
            } else {
                bytearr[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
                bytearr[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
            }
        }

        this.writeBytes(bytearr, 0, utflen + 2);
    }

    ByteBuffer finish();
}
