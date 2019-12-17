package me.jellysquid.mods.lithium.common.nbt.io.bytes;

import me.jellysquid.mods.lithium.common.nbt.io.NbtOut;

import java.nio.ByteBuffer;

public class NbtOutByteBuffer implements NbtOut {
    private ByteBuffer buf;

    public NbtOutByteBuffer(int size) {
        this.buf = ByteBuffer.allocateDirect(size);
    }

    public void ensure(int len) {
        if (this.buf.remaining() <= len) {
            int newCapacity = this.buf.limit() * 2;

            ByteBuffer old = this.buf;
            old.flip();

            this.buf = ByteBuffer.allocateDirect(newCapacity);
            this.buf.put(old);
        }
    }

    @Override
    public void writeInt(int value) {
        this.ensure(4);

        this.buf.putInt(value);
    }

    @Override
    public void writeBytes(byte[] value) {
        this.ensure(value.length);

        this.buf.put(value);
    }

    @Override
    public void writeBytes(byte[] bytes, int offset, int count) {
        this.ensure(count);

        this.buf.put(bytes, offset, count);
    }

    @Override
    public void writeByte(byte value) {
        this.ensure(1);

        this.buf.put(value);
    }

    @Override
    public void writeShort(short value) {
        this.ensure(2);

        this.buf.putShort(value);
    }

    @Override
    public void writeDouble(double value) {
        this.ensure(8);

        this.buf.putDouble(value);
    }

    @Override
    public void writeFloat(float value) {
        this.ensure(4);

        this.buf.putFloat(value);
    }

    @Override
    public void writeLong(long value) {
        this.ensure(8);

        this.buf.putLong(value);
    }

    @Override
    public byte[] finish() {
        this.buf.flip();

        byte[] copy = new byte[this.buf.limit()];

        this.buf.get(copy);

        return copy;
    }
}
