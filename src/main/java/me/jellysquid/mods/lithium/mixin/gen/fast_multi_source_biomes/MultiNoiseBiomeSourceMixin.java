package me.jellysquid.mods.lithium.mixin.gen.fast_multi_source_biomes;

import com.mojang.datafixers.util.Pair;
import me.jellysquid.mods.lithium.common.util.collections.BiomeMixedNoisePointKDTree;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BuiltinBiomes;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
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

    private BiomeMixedNoisePointKDTree kdTree;
    private Map<Biome.MixedNoisePoint, Supplier<Biome>> pointMap;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Inject(method = "<init>(JLjava/util/List;Lnet/minecraft/world/biome/source/MultiNoiseBiomeSource$NoiseParameters;Lnet/minecraft/world/biome/source/MultiNoiseBiomeSource$NoiseParameters;Lnet/minecraft/world/biome/source/MultiNoiseBiomeSource$NoiseParameters;Lnet/minecraft/world/biome/source/MultiNoiseBiomeSource$NoiseParameters;Ljava/util/Optional;)V", at = @At("RETURN"))
    public void buildKDTree(long seed, List<Pair<Biome.MixedNoisePoint, Supplier<Biome>>> biomePoints, MultiNoiseBiomeSource.NoiseParameters temperatureNoiseParameters, MultiNoiseBiomeSource.NoiseParameters humidityNoiseParameters, MultiNoiseBiomeSource.NoiseParameters altitudeNoiseParameters, MultiNoiseBiomeSource.NoiseParameters weirdnessNoiseParameters, Optional<Pair<Registry<Biome>, MultiNoiseBiomeSource.Preset>> instance, CallbackInfo ci) {
        List<Biome.MixedNoisePoint> points = new ArrayList<>(biomePoints.size());
        pointMap = new HashMap<>(biomePoints.size());
        for (var point : biomePoints) {
            points.add(point.getFirst());
            pointMap.put(point.getFirst(), point.getSecond());
        }
        this.kdTree = BiomeMixedNoisePointKDTree.build(points);
    }

    /**
     * @reason Use kd-tree to speed up nearest-neighbor biome search
     * @author magneticflux-
     */
    @Overwrite
    public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
        // [VanillaCopy] MultiNoiseBiomeSource#getBiomeForNoiseGen

        // Get the y value for perlin noise sampling. This field is always set to false in vanilla code.
        int y = this.threeDimensionalSampling ? biomeY : 0;

        // Calculate the noise point based using 4 perlin noise samplers.
        Biome.MixedNoisePoint mixedNoisePoint = new Biome.MixedNoisePoint(
                (float) this.temperatureNoise.sample(biomeX, y, biomeZ),
                (float) this.humidityNoise.sample(biomeX, y, biomeZ),
                (float) this.altitudeNoise.sample(biomeX, y, biomeZ),
                (float) this.weirdnessNoise.sample(biomeX, y, biomeZ),
                0.0F
        );

        Biome.MixedNoisePoint nearest = kdTree.nearestBiomeTo(mixedNoisePoint);
        Biome value = pointMap.get(nearest).get();
        return value != null ? value : BuiltinBiomes.THE_VOID;
    }
}
