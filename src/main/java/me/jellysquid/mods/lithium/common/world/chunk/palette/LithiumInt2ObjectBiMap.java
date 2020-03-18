package me.jellysquid.mods.lithium.common.world.chunk.palette;

import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.jellysquid.mods.lithium.common.util.math.LithiumMath;
import net.minecraft.util.collection.IndexedIterable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

import static it.unimi.dsi.fastutil.Hash.FAST_LOAD_FACTOR;

/**
 * A faster implementation of {@link net.minecraft.util.collection.Int2ObjectBiMap} which makes use of a backing map from fastutil.
 */
@SuppressWarnings("unchecked")
public class LithiumInt2ObjectBiMap<K> implements IndexedIterable<K> {
    private K[] lookupById;
    private final Object2IntMap<K> lookupByObj;
    private int size = 0;

    public LithiumInt2ObjectBiMap(int capacity) {
        this.lookupById = (K[]) new Object[capacity];
        this.lookupByObj = new Object2IntOpenHashMap<>(capacity, FAST_LOAD_FACTOR);
        this.lookupByObj.defaultReturnValue(-1);
    }

    @Override
    public K get(int id) {
        if (id >= 0 && id < this.lookupById.length) {
            return this.lookupById[id];
        }

        return null;
    }

    @Override
    public Iterator<K> iterator() {
        return Iterators.filter(Iterators.forArray(this.lookupById), Objects::nonNull);
    }

    public int getId(K obj) {
        return this.lookupByObj.getInt(obj);
    }

    public int add(K obj) {
        int id = this.size;

        if (id >= this.lookupById.length) {
            this.resize(this.size);
        }

        this.lookupByObj.put(obj, id);
        this.lookupById[id] = obj;

        this.size += 1;

        return id;
    }

    private void resize(int neededCapacity) {
        K[] prev = this.lookupById;
        this.lookupById = (K[]) new Object[LithiumMath.nextPowerOfTwo(neededCapacity + 1)];

        System.arraycopy(prev, 0, this.lookupById, 0, prev.length);
    }

    public int size() {
        return this.size;
    }

    public void clear() {
        Arrays.fill(this.lookupById, null);
        this.lookupByObj.clear();
        this.size = 0;
    }

    public boolean containsObject(K obj) {
        return this.lookupByObj.containsKey(obj);
    }
}
