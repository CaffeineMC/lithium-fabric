package me.jellysquid.mods.lithium.common.world.layer;

import net.minecraft.world.biome.layer.util.LayerFactory;
import net.minecraft.world.biome.layer.util.LayerSampler;

public final class LayerFactoryUtil {
    public static <A extends LayerSampler> LayerFactory<A> memoize(LayerFactory<A> factory) {
        return new LayerFactory<A>() {
            final ThreadLocal<A> cached = new ThreadLocal<>();

            @Override
            public A make() {
                if (this.cached.get() == null) {
                    this.cached.set(factory.make());
                }
                return this.cached.get();
            }
        };
    }
}
