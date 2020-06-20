package me.jellysquid.mods.lithium.mixin.world.layer_caching;

import me.jellysquid.mods.lithium.common.world.layer.FastCachingLayerSampler;
import net.minecraft.world.biome.layer.util.CachingLayerContext;
import net.minecraft.world.biome.layer.util.CachingLayerSampler;
import net.minecraft.world.biome.layer.util.LayerOperator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(CachingLayerContext.class)
public class MixinCachingLayerContext {
    /**
     * @reason Replace with optimized cache implementation
     * @author gegy1000
     */
    @Overwrite
    public CachingLayerSampler createSampler(LayerOperator operator) {
        return new FastCachingLayerSampler(128, operator);
    }

    /**
     * @reason Replace with optimized cache implementation
     * @author gegy1000
     */
    @Overwrite
    public CachingLayerSampler createSampler(LayerOperator operator, CachingLayerSampler sampler) {
        return new FastCachingLayerSampler(512, operator);
    }

    /**
     * @reason Replace with optimized cache implementation
     * @author gegy1000
     */
    @Overwrite
    public CachingLayerSampler createSampler(LayerOperator operator, CachingLayerSampler left, CachingLayerSampler right) {
        return new FastCachingLayerSampler(512, operator);
    }
}
