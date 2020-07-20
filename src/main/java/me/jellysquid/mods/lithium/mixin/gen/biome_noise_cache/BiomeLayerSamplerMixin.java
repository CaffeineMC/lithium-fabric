package me.jellysquid.mods.lithium.mixin.gen.biome_noise_cache;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.layer.util.CachingLayerSampler;
import net.minecraft.world.biome.layer.util.LayerFactory;
import net.minecraft.world.biome.source.BiomeLayerSampler;
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
    protected abstract Biome getBiome(int id);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(LayerFactory<CachingLayerSampler> factory, CallbackInfo ci) {
        this.tlSampler = ThreadLocal.withInitial(factory::make);
    }

    /**
     * @reason Replace with implementation that accesses the thread-local sampler
     * @author gegy1000
     */
    @Overwrite
    public Biome sample(int x, int y) {
        CachingLayerSampler tlSampler = this.tlSampler.get();
        return this.getBiome(tlSampler.sample(x, y));
    }
}
