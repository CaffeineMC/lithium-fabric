package me.jellysquid.mods.lithium.mixin.world.fast_island_noise;

import me.jellysquid.mods.lithium.common.world.noise.SimplexNoiseCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGeneratorType;
import net.minecraft.world.gen.chunk.SurfaceChunkGenerator;

@Mixin(SurfaceChunkGenerator.class)
public class MixinSurfaceChunkGenerator {
    @Shadow
    @Final
    private SimplexNoiseSampler field_24777;
    private ThreadLocal<SimplexNoiseCache> tlCache;

    @Inject(method = "<init>(Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/biome/source/BiomeSource;JLnet/minecraft/world/gen/chunk/ChunkGeneratorType;)V", at = @At("RETURN"))
    private void hookConstructor(BiomeSource biomeSource, BiomeSource biomeSource2, long l, ChunkGeneratorType chunkGeneratorType, CallbackInfo ci) {
        tlCache = ThreadLocal.withInitial(() -> new SimplexNoiseCache(this.field_24777));
    }

    /**
     * Use our fast cache instead of vanilla's uncached noise generation.
     */
    @Redirect(method = "sampleNoiseColumn([DII)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/source/TheEndBiomeSource;getNoiseAt(Lnet/minecraft/util/math/noise/SimplexNoiseSampler;II)F"))
    private float handleNoiseSample(SimplexNoiseSampler simplexNoiseSampler, int x, int z) {
        return tlCache.get().getNoiseAt(x, z);
    }
}
