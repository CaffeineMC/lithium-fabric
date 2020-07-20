package me.jellysquid.mods.lithium.common.world.layer;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.layer.util.CachingLayerSampler;
import net.minecraft.world.biome.layer.util.LayerOperator;

import java.util.Arrays;

/**
 * A much faster implementation of CachingLayerSampler which implements a fixed-size "lossy" cache.
 * This is where the main advantage in this implementation comes from: being lossy, the cache does not have to
 * clean up old entries, it does not ever have to reallocate, and the cached value will always be in the first place
 * it checks.
 *
 * It is important to note however that this is not thread-safe: accessing it from multiple threads can result in wrong
 * values being returned. This implementation works in complement with a patch to the BiomeLayerSampler that initializes
 * the biome layer stack within a thread-local
 */
public final class FastCachingLayerSampler extends CachingLayerSampler {
    private final long[] keys;
    private final int[] values;

    private final int mask;

    public FastCachingLayerSampler(int capacity, LayerOperator operator) {
        super(new Long2IntLinkedOpenHashMap(0), 0, operator);

        capacity = MathHelper.smallestEncompassingPowerOfTwo(capacity);
        this.mask = capacity - 1;

        this.keys = new long[capacity];
        Arrays.fill(this.keys, Long.MIN_VALUE);
        this.values = new int[capacity];
    }

    @Override
    public int sample(int x, int z) {
        long key = key(x, z);
        int idx = hash(key) & this.mask;

        // if the entry here has a key that matches ours, we have a cache hit
        if (this.keys[idx] == key) {
            return this.values[idx];
        }

        // cache miss: sample the operator and put the result into our cache entry
        int sampled = this.operator.apply(x, z);
        this.values[idx] = sampled;
        this.keys[idx] = key;

        return sampled;
    }

    private static int hash(long key) {
        return (int) HashCommon.mix(key);
    }

    private static long key(int x, int z) {
        return ChunkPos.toLong(x, z);
    }
}
