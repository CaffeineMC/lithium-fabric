package me.jellysquid.mods.lithium.common.nbt.io;

public interface NbtFastReader {
    int readInt();

    default void readBytes(byte[] value) {
        this.readBytes(value, value.length);
    }

    void readBytes(byte[] bytes, int len);

    byte readByte();

    short readShort();

    default double readDouble() {
        return Double.longBitsToDouble(this.readLong());
    }

    default float readFloat() {
        return Float.intBitsToFloat(this.readInt());
    }

    long readLong();

    // Mimics DataInputStream#readUTF
    default String readString() {
        int utflen = Short.toUnsignedInt(this.readShort());

        byte[] bytearr = new byte[utflen];
        char[] chararr = new char[utflen];

        int c, char2, char3;
        int count = 0;
        int chararr_count = 0;

        this.readBytes(bytearr, utflen);

        while (count < utflen) {
            c = (int) bytearr[count] & 0xff;
            if (c > 127) {
                break;
            }
            count++;
            chararr[chararr_count++] = (char) c;
        }

        while (count < utflen) {
            c = (int) bytearr[count] & 0xff;

            switch (c >> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    /* 0xxxxxxx*/
                    count++;
                    chararr[chararr_count++] = (char) c;
                    break;
                case 12:
                case 13:
                    /* 110x xxxx   10xx xxxx*/
                    count += 2;
                    if (count > utflen) {
                        throw new IllegalArgumentException(
                                "malformed input: partial character at end");
                    }
                    char2 = bytearr[count - 1];
                    if ((char2 & 0xC0) != 0x80) {
                        throw new IllegalArgumentException(
                                "malformed input around byte " + count);
                    }
                    chararr[chararr_count++] = (char) (((c & 0x1F) << 6) |
                            (char2 & 0x3F));
                    break;
                case 14:
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
                    count += 3;
                    if (count > utflen) {
                        throw new IllegalArgumentException(
                                "malformed input: partial character at end");
                    }
                    char2 = bytearr[count - 2];
                    char3 = bytearr[count - 1];
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) {
                        throw new IllegalArgumentException(
                                "malformed input around byte " + (count - 1));
                    }
                    chararr[chararr_count++] = (char) (((c & 0x0F) << 12) |
                            ((char2 & 0x3F) << 6) |
                            ((char3 & 0x3F) << 0));
                    break;
                default:
                    /* 10xx xxxx,  1111 xxxx */
                    throw new IllegalArgumentException(
                            "malformed input around byte " + count);
            }
        }

        // The number of chars produced may be less than utflen
        return new String(chararr, 0, chararr_count);
    }
}
