package me.jellysquid.mods.lithium.mixin.gen.biome_noise_cache;

import me.jellysquid.mods.lithium.common.world.layer.CachedLocalLayerFactory;
import me.jellysquid.mods.lithium.common.world.layer.CloneableContext;
import net.minecraft.world.biome.layer.type.InitLayer;
import net.minecraft.world.biome.layer.util.LayerFactory;
import net.minecraft.world.biome.layer.util.LayerSampleContext;
import net.minecraft.world.biome.layer.util.LayerSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Memoize the LayerFactory and make it produce thread-local copies for thread-safety purposes
 */
@Mixin(InitLayer.class)
public interface InitLayerMixin extends InitLayer {
    /**
     * @reason Replace with a memoized and thread-local layer factory
     * @author gegy1000
     */
    @Overwrite
    @SuppressWarnings("unchecked")
    default <R extends LayerSampler> LayerFactory<R> create(LayerSampleContext<R> context) {
        return CachedLocalLayerFactory.createInit(this, (CloneableContext<R>) context);
    }
}