package me.jellysquid.mods.lithium.common.world.layer;

import net.minecraft.world.biome.layer.type.ParentedLayer;
import net.minecraft.world.biome.layer.util.LayerFactory;
import net.minecraft.world.biome.layer.util.LayerSampleContext;
import net.minecraft.world.biome.layer.util.LayerSampler;

public class CachedLocalLayerFactory<R extends LayerSampler> implements LayerFactory<R> {
    private final ThreadLocal<R> cached = new ThreadLocal<>();

    private final ParentedLayer layer;
    private final LayerSampleContext<R> context;
    private final LayerFactory<R> parent;

    public CachedLocalLayerFactory(ParentedLayer layer, LayerSampleContext<R> context, LayerFactory<R> parent) {
        this.layer = layer;
        this.context = context;
        this.parent = parent;
    }

    @Override
    public R make() {
        R sampler = this.cached.get();

        if (sampler == null) {
            this.cached.set(sampler = createLocalSampler());
        }

        return sampler;
    }

    private R createLocalSampler() {
        R layerSampler = this.parent.make();

        return this.context.createSampler((x, z) -> {
            this.context.initSeed(x, z);

            return CachedLocalLayerFactory.this.layer.sample(this.context, layerSampler, x, z);
        }, layerSampler);
    }
}