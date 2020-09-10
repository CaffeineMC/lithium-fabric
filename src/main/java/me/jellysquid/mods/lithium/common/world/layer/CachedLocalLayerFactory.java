package me.jellysquid.mods.lithium.common.world.layer;

import net.minecraft.world.biome.layer.type.InitLayer;
import net.minecraft.world.biome.layer.type.MergingLayer;
import net.minecraft.world.biome.layer.type.ParentedLayer;
import net.minecraft.world.biome.layer.util.LayerFactory;
import net.minecraft.world.biome.layer.util.LayerSampleContext;
import net.minecraft.world.biome.layer.util.LayerSampler;

public final class CachedLocalLayerFactory {
    public static <R extends LayerSampler> LayerFactory<R> createInit(InitLayer layer, CloneableContext<R> context) {
        return createMemoized(() -> {
            LayerSampleContext<R> clonedContext = context.cloneContext();
            return clonedContext.createSampler((x, z) -> {
                clonedContext.initSeed(x, z);
                return layer.sample(clonedContext, x, z);
            });
        });
    }

    public static <R extends LayerSampler> LayerFactory<R> createParented(ParentedLayer layer, CloneableContext<R> context, LayerFactory<R> parent) {
        return createMemoized(() -> {
            LayerSampleContext<R> clonedContext = context.cloneContext();
            R parentSampler = parent.make();

            return clonedContext.createSampler((x, z) -> {
                clonedContext.initSeed(x, z);
                return layer.sample(clonedContext, parentSampler, x, z);
            }, parentSampler);
        });
    }

    public static <R extends LayerSampler> LayerFactory<R> createMerging(MergingLayer layer, CloneableContext<R> context, LayerFactory<R> layer1, LayerFactory<R> layer2) {
        return createMemoized(() -> {
            LayerSampleContext<R> clonedContext = context.cloneContext();
            R sampler1 = layer1.make();
            R sampler2 = layer2.make();

            return clonedContext.createSampler((x, z) -> {
                clonedContext.initSeed(x, z);
                return layer.sample(clonedContext, sampler1, sampler2, x, z);
            }, sampler1, sampler2);
        });
    }

    private static <R extends LayerSampler> LayerFactory<R> createMemoized(LayerFactory<R> factory) {
        ThreadLocal<R> threadLocal = ThreadLocal.withInitial(factory::make);
        return threadLocal::get;
    }
}