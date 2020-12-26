package me.jellysquid.mods.lithium.common.world.biome;

import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.biome.source.BiomeSource;

/**
 * Biome array that samples in the horizontal axis and uses that to fill in the vertical axis, skipping a majority of calls to the biome source.
 * This should only be used when the dimension's biome access type is also horizontal.
 */
public class HorizontalBiomeArray extends BiomeArray {
    private static final int HORIZONTAL_SECTION_COUNT = (int)Math.round(Math.log(16.0D) / Math.log(2.0D)) - 2;
    private static final int HORIZONTAL_BITS = HORIZONTAL_SECTION_COUNT * 2;

    public HorizontalBiomeArray(IndexedIterable<Biome> indexedIterable, ChunkPos pos, BiomeSource source) {
        super(indexedIterable, sampleBiomes(pos, source));
    }

    private static Biome[] sampleBiomes(ChunkPos pos, BiomeSource source) {
        // Get the start of the biome sampling section, which is 4x4 blocks wide
        int startX = pos.getStartX() >> 2;
        int startZ = pos.getStartZ() >> 2;

        Biome[] biomes = new Biome[DEFAULT_LENGTH];

        // Iterate from 0 to 16
        for (int index = 0; index < (1 << HORIZONTAL_BITS); ++index) {
            // Mask the bottom 4 to get the x coord
            int biomeX = index & HORIZONTAL_BIT_MASK;
            // Move the top 4 bits and the mask to get the z coord
            int biomeZ = (index >> HORIZONTAL_SECTION_COUNT) & HORIZONTAL_BIT_MASK;

            // Sample the biome at this position
            Biome biome = source.getBiomeForNoiseGen(startX + biomeX, 0, startZ + biomeZ);

            // Fill in all of the vertical bits with the biome
            for (int y = 0; y <= VERTICAL_BIT_MASK; y++) {
                biomes[index | (y << HORIZONTAL_BITS)] = biome;
            }
        }

        return biomes;
    }
}
