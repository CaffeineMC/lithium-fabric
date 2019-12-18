package me.jellysquid.mods.lithium.common.nbt.io.bytes;

import me.jellysquid.mods.lithium.common.nbt.io.NbtFastReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class NbtByteArrayReader implements NbtFastReader {
    private final ByteBuffer buf;

    public NbtByteArrayReader(byte[] data) {
        this.buf = ByteBuffer.wrap(data);
        this.buf.order(ByteOrder.BIG_ENDIAN);
    }

    @Override
    public int readInt() {
        return this.buf.getInt();
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
    public long readLong() {
        return this.buf.getLong();
    }
}
