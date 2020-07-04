package me.jellysquid.mods.lithium.mixin.gen.biome_noise_cache;

import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import me.jellysquid.mods.lithium.common.world.layer.CloneableContext;
import me.jellysquid.mods.lithium.common.world.layer.FastCachingLayerSampler;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.world.biome.layer.util.CachingLayerContext;
import net.minecraft.world.biome.layer.util.CachingLayerSampler;
import net.minecraft.world.biome.layer.util.LayerOperator;
import net.minecraft.world.biome.layer.util.LayerSampleContext;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CachingLayerContext.class)
public class CachingLayerContextMixin implements CloneableContext<CachingLayerSampler> {
    @Shadow
    @Final
    @Mutable
    private long worldSeed;

    @Shadow
    @Final
    @Mutable
    private PerlinNoiseSampler noiseSampler;

    @Shadow
    @Final
    @Mutable
    private Long2IntLinkedOpenHashMap cache;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(int cacheCapacity, long seed, long salt, CallbackInfo ci) {
        // we don't use this cache
        this.cache = null;
    }

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

    @Override
    public LayerSampleContext<CachingLayerSampler> cloneContext() {
        CachingLayerContext context = new CachingLayerContext(0, 0, 0);

        CachingLayerContextMixin access = (CachingLayerContextMixin) (Object) context;
        access.worldSeed = this.worldSeed;
        access.noiseSampler = this.noiseSampler;

        return context;
    }
}
