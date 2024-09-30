package net.caffeinemc.mods.lithium.common;

import net.caffeinemc.mods.lithium.common.config.LithiumConfig;
import net.fabricmc.api.ModInitializer;

public class LithiumMod implements ModInitializer {
    public static LithiumConfig CONFIG;

    @Override
    public void onInitialize() {
        if (CONFIG == null) {
            throw new IllegalStateException("The mixin plugin did not initialize the config! Did it not load?");
        }
    }
}
