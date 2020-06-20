package me.jellysquid.mods.lithium.mixin.world.layer_caching;

import me.jellysquid.mods.lithium.common.world.layer.LayerFactoryUtil;
import net.minecraft.world.biome.layer.*;
import net.minecraft.world.biome.layer.type.ParentedLayer;
import net.minecraft.world.biome.layer.util.LayerFactory;
import net.minecraft.world.biome.layer.util.LayerSampleContext;
import net.minecraft.world.biome.layer.util.LayerSampler;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Memoize the LayerFactory created by the ParentedLayer such that layers don't get duplicated when accessed from
 * multiple children layers
 *
 * Because we can't apply mixin to default method, just override in classes that get duplicated
 */
@Mixin({
        ScaleLayer.class,
        AddDeepOceanLayer.class,
        SimpleLandNoiseLayer.class,
        SmoothenShorelineLayer.class,
})
public abstract class MixinParentedLayer implements ParentedLayer {
    @Override
    public <R extends LayerSampler> LayerFactory<R> create(LayerSampleContext<R> context, LayerFactory<R> parent) {
        return LayerFactoryUtil.memoize(() -> {
            R layerSampler = parent.make();
            return context.createSampler((x, z) -> {
                context.initSeed(x, z);
                return this.sample(context, layerSampler, x, z);
            }, layerSampler);
        });
    }
}
