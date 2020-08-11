package me.jellysquid.mods.lithium.mixin.gen.biome_noise_cache;

import net.minecraft.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.layer.util.CachingLayerSampler;
import net.minecraft.world.biome.layer.util.LayerFactory;
import net.minecraft.world.biome.source.BiomeLayerSampler;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BiomeLayerSampler.class)
public abstract class BiomeLayerSamplerMixin {
    private ThreadLocal<CachingLayerSampler> tlSampler;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(LayerFactory<CachingLayerSampler> factory, CallbackInfo ci) {
        this.tlSampler = ThreadLocal.withInitial(factory::make);
    }

    /**
     * @reason Replace with implementation that accesses the thread-local sampler
     * @author gegy1000
     */
    @Overwrite
    public Biome sample(Registry<Biome> registry, int i, int j) {
        // [VanillaCopy]
        CachingLayerSampler tlSampler = this.tlSampler.get();

        int k = tlSampler.sample(i, j);
        RegistryKey<Biome> registryKey = Biomes.fromRawId(k);
        if (registryKey == null) {
            throw new IllegalStateException("Unknown biome id emitted by layers: " + k);
        } else {
            Biome biome = (Biome)registry.get(registryKey);
            if (biome == null) {
                if (SharedConstants.isDevelopment) {
                    throw (IllegalStateException) Util.throwOrPause(new IllegalStateException("Unknown biome id: " + k));
                } else {
                    LOGGER.warn("Unknown biome id: ", k);
                    return (Biome)registry.get(Biomes.fromRawId(0));
                }
            } else {
                return biome;
            }
        }
    }
}
