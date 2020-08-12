package me.jellysquid.mods.lithium.mixin.world.fast_island_noise;

import me.jellysquid.mods.lithium.common.world.noise.SimplexNoiseCache;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.world.biome.source.BiomeSource;
import java.util.function.Supplier;

@Mixin(NoiseChunkGenerator.class)
public class MixinNoiseChunkGenerator {
    @Shadow
    @Final
    private SimplexNoiseSampler islandNoise;
    private ThreadLocal<SimplexNoiseCache> tlCache;

    @Inject(method = "<init>(Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/biome/source/BiomeSource;JLjava/util/function/Supplier;)V", at = @At("RETURN"))
    private void hookConstructor(BiomeSource biomeSource, BiomeSource biomeSource2, long worldSeed, Supplier<ChunkGeneratorSettings> supplier, CallbackInfo ci) {
        tlCache = ThreadLocal.withInitial(() -> new SimplexNoiseCache(this.islandNoise));
    }

    /**
     * Use our fast cache instead of vanilla's uncached noise generation.
     */
    @Redirect(method = "sampleNoiseColumn([DII)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/source/TheEndBiomeSource;getNoiseAt(Lnet/minecraft/util/math/noise/SimplexNoiseSampler;II)F"))
    private float handleNoiseSample(SimplexNoiseSampler simplexNoiseSampler, int x, int z) {
        return tlCache.get().getNoiseAt(x, z);
    }
}
