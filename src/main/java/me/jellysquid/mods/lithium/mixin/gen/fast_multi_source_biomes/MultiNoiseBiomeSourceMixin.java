package me.jellysquid.mods.lithium.mixin.gen.fast_multi_source_biomes;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BuiltinBiomes;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.Supplier;

@Mixin(MultiNoiseBiomeSource.class)
public class MultiNoiseBiomeSourceMixin {
    @Shadow
    @Final
    private boolean threeDimensionalSampling;

    @Shadow
    @Final
    private DoublePerlinNoiseSampler temperatureNoise;

    @Shadow
    @Final
    private DoublePerlinNoiseSampler humidityNoise;

    @Shadow
    @Final
    private DoublePerlinNoiseSampler weirdnessNoise;

    @Shadow
    @Final
    private DoublePerlinNoiseSampler altitudeNoise;

    @Shadow
    @Final
    private List<Pair<Biome.MixedNoisePoint, Supplier<Biome>>> biomePoints;

    /**
     * @reason Remove stream based code in favor of regular collections.
     * @author SuperCoder79
     */
    @Overwrite
    public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
        // [VanillaCopy] MultiNoiseBiomeSource#getBiomeForNoiseGen

        // Get the y value for perlin noise sampling. This field is always set to false in vanilla code.
        int y = threeDimensionalSampling ? biomeY : 0;

        // Calculate the noise point based using 4 perlin noise samplers.
        Biome.MixedNoisePoint mixedNoisePoint = new Biome.MixedNoisePoint(
                (float) this.temperatureNoise.sample(biomeX, y, biomeZ),
                (float) this.humidityNoise.sample(biomeX, y, biomeZ),
                (float) this.altitudeNoise.sample(biomeX, y, biomeZ),
                (float) this.weirdnessNoise.sample(biomeX, y, biomeZ),
                0.0F);

        int idx = -1;
        float min = Float.POSITIVE_INFINITY;

        // Iterate through the biome points and calculate the distance to the current noise point.
        for (int i = 0; i < biomePoints.size(); i++) {
            float distance = biomePoints.get(i).getFirst().calculateDistanceTo(mixedNoisePoint);

            // If the distance is less than the recorded minimum, update the minimum and set the current index.
            if (min > distance) {
                idx = i;
                min = distance;
            }
        }

        // Return the biome with the noise point closest to the evaluated one.
        return biomePoints.get(idx).getSecond().get() == null ? BuiltinBiomes.THE_VOID : biomePoints.get(idx).getSecond().get();
    }
}
