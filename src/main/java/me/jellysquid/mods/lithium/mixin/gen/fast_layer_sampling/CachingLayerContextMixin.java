package me.jellysquid.mods.lithium.mixin.gen.fast_layer_sampling;

import me.jellysquid.mods.lithium.common.world.layer.CachingLayerContextExtended;
import net.minecraft.world.biome.layer.util.CachingLayerContext;
import net.minecraft.world.biome.source.SeedMixer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CachingLayerContext.class)
public class CachingLayerContextMixin implements CachingLayerContextExtended {
    @Shadow
    private long localSeed;

    @Shadow
    @Final
    private long worldSeed;

    @Override
    public void skipInt() {
        this.localSeed = SeedMixer.mixSeed(this.localSeed, this.worldSeed);
    }
}