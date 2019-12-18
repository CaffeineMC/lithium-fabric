package me.jellysquid.mods.lithium.common.nbt.io;

public interface NbtFastWriter {
    void writeInt(int value);

    default void writeByteArray(byte[] bytes) {
        this.writeByteArray(bytes, 0, bytes.length);
    }

    void writeByteArray(byte[] bytes, int offset, int count);

    void writeByte(byte value);

    void writeShort(short value);

    default void writeDouble(double value) {
        this.writeLong(Double.doubleToLongBits(value));
    }

    default void writeFloat(float value) {
        this.writeInt(Float.floatToIntBits(value));
    }

    void writeLong(long value);

    void writeLongArray(long[] values);

    // Mimics DataOutputStream#writeUTF
    void writeString(String str);

    byte[] finish();
}
