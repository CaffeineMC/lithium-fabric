package me.jellysquid.mods.lithium.mixin.gen.biome_noise_cache;

import me.jellysquid.mods.lithium.common.world.layer.CachedLocalLayerFactory;
import me.jellysquid.mods.lithium.common.world.layer.CloneableContext;
import net.minecraft.world.biome.layer.type.ParentedLayer;
import net.minecraft.world.biome.layer.util.LayerFactory;
import net.minecraft.world.biome.layer.util.LayerSampleContext;
import net.minecraft.world.biome.layer.util.LayerSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Memoize the LayerFactory created by the ParentedLayer such that layers don't get duplicated when accessed from
 * multiple children layers.
 */
@Mixin(ParentedLayer.class)
public interface ParentedLayerMixin extends ParentedLayer {
    /**
     * @reason Replace with a memoized layer factory
     * @author gegy1000
     */
    @Overwrite
    @SuppressWarnings("unchecked")
    default <R extends LayerSampler> LayerFactory<R> create(LayerSampleContext<R> context, LayerFactory<R> parent) {
        return new CachedLocalLayerFactory<>(this, (CloneableContext<R>) context, parent);
    }
}