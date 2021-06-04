package me.jellysquid.mods.lithium.mixin.gen.fast_island_noise;

import me.jellysquid.mods.lithium.common.world.noise.SimplexNoiseCache;
import net.minecraft.util.math.noise.InterpolatedNoiseSampler;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.NoiseColumnSampler;
import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import net.minecraft.world.gen.chunk.WeightSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseColumnSampler.class)
public class NoiseColumnSamplerMixin {
    @Shadow
    @Final
    private SimplexNoiseSampler islandNoise;

    private ThreadLocal<SimplexNoiseCache> tlCache;

    @Inject(method = "<init>(Lnet/minecraft/world/biome/source/BiomeSource;IIILnet/minecraft/world/gen/chunk/GenerationShapeConfig;Lnet/minecraft/util/math/noise/InterpolatedNoiseSampler;Lnet/minecraft/util/math/noise/SimplexNoiseSampler;Lnet/minecraft/util/math/noise/OctavePerlinNoiseSampler;Lnet/minecraft/world/gen/chunk/WeightSampler;)V", at = @At("RETURN"))
    private void hookConstructor(BiomeSource biomeSource, int horizontalNoiseResolution, int verticalNoiseResolution, int noiseSizeY, GenerationShapeConfig config, InterpolatedNoiseSampler noise, SimplexNoiseSampler islandNoise, OctavePerlinNoiseSampler densityNoise, WeightSampler weightSampler, CallbackInfo ci) {
        this.tlCache = ThreadLocal.withInitial(() -> new SimplexNoiseCache(this.islandNoise));
    }

    /**
     * Use our fast cache instead of vanilla's uncached noise generation.
     */
    @Redirect(
            method = "sampleNoiseColumn([DIILnet/minecraft/world/gen/chunk/GenerationShapeConfig;III)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/biome/source/TheEndBiomeSource;getNoiseAt(Lnet/minecraft/util/math/noise/SimplexNoiseSampler;II)F"
            )
    )
    private float handleNoiseSample(SimplexNoiseSampler simplexNoiseSampler, int x, int z) {
        return this.tlCache.get().getNoiseAt(x, z);
    }
}
