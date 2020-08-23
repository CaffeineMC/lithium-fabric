package me.jellysquid.mods.lithium.mixin.gen.biome_noise_cache;

import net.minecraft.world.biome.layer.util.CachingLayerSampler;
import net.minecraft.world.biome.layer.util.LayerFactory;
import net.minecraft.world.biome.source.BiomeLayerSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BiomeLayerSampler.class)
public abstract class BiomeLayerSamplerMixin {
    private ThreadLocal<CachingLayerSampler> tlSampler;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(LayerFactory<CachingLayerSampler> factory, CallbackInfo ci) {
        this.tlSampler = ThreadLocal.withInitial(factory::make);
    }

    /**
     * @reason Replace with implementation that accesses the thread-local sampler
     * @author gegy1000
     * original implementation by gegy1000, 2No2Name replaced @Overwrite with @Redirect
     */
    @Overwrite
    public Biome sample(Registry<Biome> registry, int i, int j) {
        // [VanillaCopy]
        final CachingLayerSampler tlSampler = this.tlSampler.get();

        final int k = tlSampler.sample(i, j);
        final RegistryKey<Biome> registryKey = BuiltinBiomes.fromRawId(k);
        if (registryKey == null) {
            throw new IllegalStateException("Unknown biome id emitted by layers: " + k);
        } else {
            final Biome biome = registry.get(registryKey);
            if (biome == null) {
                if (SharedConstants.isDevelopment) {
                    throw Util.throwOrPause(new IllegalStateException("Unknown biome id: " + k));
                } else {
                    LOGGER.warn("Unknown biome id: {}", k);
                    return registry.get(BuiltinBiomes.fromRawId(0));
                }
            } else {
                return biome;
            }
        
          
    @Redirect(method = "sample", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/layer/util/CachingLayerSampler;sample(II)I"))
    private int sampleThreadLocal(CachingLayerSampler cachingLayerSampler, int i, int j) {
        return this.tlSampler.get().sample(i,j);
    }
}
