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
import net.minecraft.world.biome.source.TheEndBiomeSource;

@Mixin(TheEndBiomeSource.class)
public class MixinTheEndBiomeSource {
    @Shadow
    @Final
    private SimplexNoiseSampler noise;
    private ThreadLocal<SimplexNoiseCache> tlCache;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void hookConstructor(long seed, CallbackInfo ci) {
        tlCache = ThreadLocal.withInitial(() -> new SimplexNoiseCache(this.noise));
    }

    /**
     * Use our fast cache instead of vanilla's uncached noise generation.
     */
    @Redirect(method = "getBiomeForNoiseGen", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/source/TheEndBiomeSource;getNoiseAt(Lnet/minecraft/util/math/noise/SimplexNoiseSampler;II)F"))
    private float handleNoiseSample(SimplexNoiseSampler simplexNoiseSampler, int x, int z) {
        return tlCache.get().getNoiseAt(x, z);
    }
}
