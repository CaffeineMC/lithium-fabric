package me.jellysquid.mods.lithium.common.world.chunk.palette;

/**
 * Due to the package-private nature of {@link net.minecraft.world.chunk.PaletteResizeListener}, this re-implements
 * the aforementioned interface publicly.
 */
public interface LithiumPaletteResizeListener<T> {
    int onLithiumPaletteResized(int size, T obj);

}
