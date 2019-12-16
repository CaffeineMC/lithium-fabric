package me.jellysquid.mods.lithium.common;

import me.jellysquid.mods.lithium.common.config.LithiumConfig;
import me.jellysquid.mods.lithium.common.shapes.merging.IndirectListPairCache;
import net.fabricmc.api.ModInitializer;

public class LithiumMod implements ModInitializer {
    public static LithiumConfig CONFIG;

    @Override
    public void onInitialize() {
        if (CONFIG == null) {
            throw new IllegalStateException("The mixin plugin did not initialize the config! Did it not load?");
        }

        IndirectListPairCache.init();
    }
}
