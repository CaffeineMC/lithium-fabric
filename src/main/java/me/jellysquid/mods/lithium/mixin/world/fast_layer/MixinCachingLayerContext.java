package me.jellysquid.mods.lithium.mixin.world.fast_layer;

import me.jellysquid.mods.lithium.common.world.layer.CachingLayerContextExtended;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.world.biome.layer.util.CachingLayerContext;
import net.minecraft.world.biome.source.SeedMixer;

@Mixin(CachingLayerContext.class)
public class MixinCachingLayerContext implements CachingLayerContextExtended {
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