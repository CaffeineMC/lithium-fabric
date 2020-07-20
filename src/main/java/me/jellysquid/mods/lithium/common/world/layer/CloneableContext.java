package me.jellysquid.mods.lithium.common.world.layer;

import net.minecraft.world.biome.layer.util.LayerSampleContext;
import net.minecraft.world.biome.layer.util.LayerSampler;

public interface CloneableContext<R extends LayerSampler> {
    LayerSampleContext<R> cloneContext();
}
