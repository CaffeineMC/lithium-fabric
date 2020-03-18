package me.jellysquid.mods.lithium.common.world.chunk.palette;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.collection.IdList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.chunk.Palette;

import java.util.function.Function;

/**
 * Generally provides better performance over the vanilla {@link net.minecraft.world.chunk.BiMapPalette} when calling
 * {@link LithiumHashPalette#getIndex(Object)} through using a faster backing map.
 */
public class LithiumHashPalette<T> implements Palette<T> {
    private final IdList<T> idList;
    private final LithiumInt2ObjectBiMap<T> map;
    private final LithiumPaletteResizeListener<T> resizeHandler;
    private final Function<CompoundTag, T> elementDeserializer;
    private final Function<T, CompoundTag> elementSerializer;
    private final int indexBits;

    public LithiumHashPalette(IdList<T> ids, int bits, LithiumPaletteResizeListener<T> resizeHandler, Function<CompoundTag, T> deserializer, Function<T, CompoundTag> serializer) {
        this.idList = ids;
        this.indexBits = bits;
        this.resizeHandler = resizeHandler;
        this.elementDeserializer = deserializer;
        this.elementSerializer = serializer;
        this.map = new LithiumInt2ObjectBiMap<>(1 << bits);
    }

    @Override
    public int getIndex(T obj) {
        int id = this.map.getId(obj);

        if (id == -1) {
            id = this.map.add(obj);

            if (id >= 1 << this.indexBits) {
                if (this.resizeHandler == null) {
                    throw new IllegalStateException("Cannot grow");
                } else {
                    id = this.resizeHandler.onLithiumPaletteResized(this.indexBits + 1, obj);
                }
            }
        }

        return id;
    }

    @Override
    public boolean accepts(T obj) {
        return this.map.containsObject(obj);
    }

    @Override
    public T getByIndex(int id) {
        return this.map.get(id);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void fromPacket(PacketByteBuf buf) {
        this.map.clear();
        int entryCount = buf.readVarInt();

        for (int i = 0; i < entryCount; ++i) {
            this.map.add(this.idList.get(buf.readVarInt()));
        }
    }

    @Override
    public void toPacket(PacketByteBuf buf) {
        int paletteBits = this.getSize();
        buf.writeVarInt(paletteBits);

        for (int i = 0; i < paletteBits; ++i) {
            buf.writeVarInt(this.idList.getId(this.map.get(i)));
        }
    }

    @Override
    public int getPacketSize() {
        int size = PacketByteBuf.getVarIntSizeBytes(this.getSize());

        for (int i = 0; i < this.getSize(); ++i) {
            size += PacketByteBuf.getVarIntSizeBytes(this.idList.getId(this.map.get(i)));
        }

        return size;
    }

    @Override
    public void fromTag(ListTag list) {
        this.map.clear();

        for (int i = 0; i < list.size(); ++i) {
            this.map.add(this.elementDeserializer.apply(list.getCompound(i)));
        }
    }

    public void toTag(ListTag list) {
        for (int i = 0; i < this.getSize(); ++i) {
            list.add(this.elementSerializer.apply(this.map.get(i)));
        }
    }

    public int getSize() {
        return this.map.size();
    }
}
