package me.jellysquid.mods.lithium.common.world.noise;

import java.util.Arrays;

import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.SimplexNoiseSampler;

/**
 * A cache for the End's noise generator that caches computed values. Through the caching, we can eliminate a large
 * amount of overhead in computing the noise values several hundred thousand times per chunk. This code uses the same
 * array backed lossy cache as FastCachingLayerSampler.
 */
public class SimplexNoiseCache {
    private static final int GRID_SIZE = 2;
    private static final float MIN = -100.0F;
    private static final float MAX = 80.0F;
    private static final float ISLAND_RADIUS = 100.0F;
    // The smallest encompassing power of two that can store all of the noise values in a chunk.
    private static final int CACHE_SIZE = 8192;

    private final int mask;
    private final long[] keys;
    private final float[] values;

    private final SimplexNoiseSampler sampler;

    public SimplexNoiseCache(SimplexNoiseSampler sampler) {
        this.sampler = sampler;

        this.mask = CACHE_SIZE - 1;

        // Initialize default values
        this.keys = new long[CACHE_SIZE];
        Arrays.fill(this.keys, Long.MIN_VALUE);
        this.values = new float[CACHE_SIZE];
    }

    /**
     * Attempt to get the cached distance factor, saving computation time.
     */
    private float getDistanceFactor(int x, int z) {
        // Hash key and get index
        long key = ChunkPos.toLong(x, z);
        int idx = (int) HashCommon.mix(key) & this.mask;

        if (this.keys[idx] == key) {
            // Cache hit, return cached value
            return this.values[idx];
        }

        // Cache miss, compute and store value

        // A marker for no value.
        float value = -1.0F;

        long lx = x;
        long lz = z;
        long distanceFromOriginSq = lx * lx + lz * lz;

        // Ensure we are 64 grid cells away from the origin.
        if (distanceFromOriginSq > 64 * 64) {
            // Reduce the number of island-forming grid cells by sampling noise with a threshold
            if (this.sampler.sample(x, z) < -0.9) {
                // Generate a pseudo-random value from 9 to 21
                value = (MathHelper.abs(x) * 3439.0F + MathHelper.abs(z) * 147.0F) % 13.0F + 9.0F;
            }
        }

        // Store values in cache
        this.keys[idx] = key;
        this.values[idx] = value;

        return value;
    }

    /**
     * Mapped and cleaned up implementation of the End biome source's sampler. Tries to use cached values wherever possible.
     */
    public float getNoiseAt(int x, int z) {
        // [VanillaCopy] TheEndBiomeSource#getNoiseAt

        int gridX = x / GRID_SIZE;
        int gridZ = z / GRID_SIZE;

        // This is the "center point", offset to center around the current grid cell
        int gridOriginX = x % GRID_SIZE;
        int gridOriginZ = z % GRID_SIZE;

        // Initialize density for the central island
        float density = ISLAND_RADIUS - MathHelper.sqrt(x * x + z * z) * 8.0F;
        if (density >= MAX) {
            return MAX;
        }

        // Iterate through 25x25 grid cells
        for (int offsetX = -12; offsetX <= 12; ++offsetX) {
            for (int offsetZ = -12; offsetZ <= 12; ++offsetZ) {
                int globalGridX = gridX + offsetX;
                int globalGridZ = gridZ + offsetZ;

                // Try to retrieve values from cache
                float distanceFactor = getDistanceFactor(globalGridX, globalGridZ);
                if (distanceFactor != -1.0F) {
                    // Compute the distance to the origin
                    float deltaX = gridOriginX - offsetX * GRID_SIZE;
                    float deltaZ = gridOriginZ - offsetZ * GRID_SIZE;

                    // Calculate the density at this grid cell
                    float scaledDistance = MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ) * distanceFactor;
                    float densityHere = ISLAND_RADIUS - scaledDistance;

                    // Try to return early if we're over the max
                    if (densityHere > density) {
                        if (densityHere >= MAX) {
                            return MAX;
                        }

                        density = densityHere;
                    }
                }
            }
        }

        // Avoid a call to Math.max because the density cannot be bigger than the max.
        if (density < MIN) {
            return MIN;
        }

        return density;
    }
}
