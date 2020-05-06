package me.jellysquid.mods.lithium.common.world.chunk;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.IdList;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PaletteResizeListener;

import java.util.Arrays;
import java.util.function.Function;

import static it.unimi.dsi.fastutil.Hash.FAST_LOAD_FACTOR;

/**
 * Generally provides better performance over the vanilla {@link net.minecraft.world.chunk.BiMapPalette} when calling
 * {@link LithiumHashPalette#getIndex(Object)} through using a faster backing map and reducing pointer chasing.
 */
public class LithiumHashPalette<T> implements Palette<T> {
    private final IdList<T> idList;
    private final PaletteResizeListener<T> resizeHandler;
    private final Function<CompoundTag, T> elementDeserializer;
    private final Function<T, CompoundTag> elementSerializer;
    private final int indexBits;

    private T[] entries;
    private final Object2IntMap<T> table;
    private int size = 0;

    @SuppressWarnings("unchecked")
    public LithiumHashPalette(IdList<T> ids, int bits, PaletteResizeListener<T> resizeHandler, Function<CompoundTag, T> deserializer, Function<T, CompoundTag> serializer) {
        this.idList = ids;
        this.indexBits = bits;
        this.resizeHandler = resizeHandler;
        this.elementDeserializer = deserializer;
        this.elementSerializer = serializer;

        int capacity = 1 << bits;

        this.entries = (T[]) new Object[capacity];
        this.table = new Object2IntOpenHashMap<>(capacity, FAST_LOAD_FACTOR);
        this.table.defaultReturnValue(-1);
    }

    @Override
    public int getIndex(T obj) {
        int id = this.table.getInt(obj);

        if (id == -1) {
            id = this.addEntry(obj);

            if (id >= 1 << this.indexBits) {
                if (this.resizeHandler == null) {
                    throw new IllegalStateException("Cannot grow");
                } else {
                    id = this.resizeHandler.onResize(this.indexBits + 1, obj);
                }
            }
        }

        return id;
    }

    private int addEntry(T obj) {
        int id = this.size;

        if (id >= this.entries.length) {
            this.resize(this.size);
        }

        this.table.put(obj, id);
        this.entries[id] = obj;

        this.size += 1;

        return id;
    }

    @SuppressWarnings("unchecked")
    private void resize(int neededCapacity) {
        T[] prev = this.entries;
        this.entries = (T[]) new Object[HashCommon.nextPowerOfTwo(neededCapacity + 1)];

        System.arraycopy(prev, 0, this.entries, 0, prev.length);
    }

    @Override
    public boolean accepts(T obj) {
        return this.table.containsKey(obj);
    }

    @Override
    public T getByIndex(int id) {
        if (id >= 0 && id < this.entries.length) {
            return this.entries[id];
        }

        return null;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void fromPacket(PacketByteBuf buf) {
        this.clear();

        int entryCount = buf.readVarInt();

        for (int i = 0; i < entryCount; ++i) {
            this.addEntry(this.idList.get(buf.readVarInt()));
        }
    }

    @Override
    public void toPacket(PacketByteBuf buf) {
        int paletteBits = this.getSize();
        buf.writeVarInt(paletteBits);

        for (int i = 0; i < paletteBits; ++i) {
            buf.writeVarInt(this.idList.getId(this.getByIndex(i)));
        }
    }

    @Override
    public int getPacketSize() {
        int size = PacketByteBuf.getVarIntSizeBytes(this.getSize());

        for (int i = 0; i < this.getSize(); ++i) {
            size += PacketByteBuf.getVarIntSizeBytes(this.idList.getId(this.getByIndex(i)));
        }

        return size;
    }

    @Override
    public void fromTag(ListTag list) {
        this.clear();

        for (int i = 0; i < list.size(); ++i) {
            this.addEntry(this.elementDeserializer.apply(list.getCompound(i)));
        }
    }

    public void toTag(ListTag list) {
        for (int i = 0; i < this.getSize(); ++i) {
            list.add(this.elementSerializer.apply(this.getByIndex(i)));
        }
    }

    public int getSize() {
        return this.size;
    }

    private void clear() {
        Arrays.fill(this.entries, null);
        this.table.clear();
        this.size = 0;
    }
}
