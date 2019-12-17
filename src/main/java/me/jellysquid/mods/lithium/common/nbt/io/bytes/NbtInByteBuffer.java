package me.jellysquid.mods.lithium.common.nbt.io.bytes;

import me.jellysquid.mods.lithium.common.nbt.io.NbtIn;

import java.nio.ByteBuffer;

public class NbtInByteBuffer implements NbtIn {
    private final ByteBuffer buf;

    public NbtInByteBuffer(ByteBuffer buf) {
        this.buf = buf;
    }

    @Override
    public int readInt() {
        return this.buf.getInt();
    }

    @Override
    public void readBytes(byte[] value) {
        this.buf.get(value);
    }

    @Override
    public void readBytes(byte[] bytes, int len) {
        this.buf.get(bytes, 0, len);
    }

    @Override
    public byte readByte() {
        return this.buf.get();
    }

    @Override
    public short readShort() {
        return this.buf.getShort();
    }

    @Override
    public char readChar() {
        return this.buf.getChar();
    }

    @Override
    public double readDouble() {
        return this.buf.getDouble();
    }

    @Override
    public float readFloat() {
        return this.buf.getFloat();
    }

    @Override
    public long readLong() {
        return this.buf.getLong();
    }
}
