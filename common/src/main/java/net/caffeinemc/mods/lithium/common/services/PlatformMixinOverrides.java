package net.caffeinemc.mods.lithium.common.services;

import net.caffeinemc.mods.lithium.common.config.Option;

import java.util.List;
import java.util.Map;

public interface PlatformMixinOverrides {
    PlatformMixinOverrides INSTANCE = Services.load(PlatformMixinOverrides.class);

    static PlatformMixinOverrides getInstance() {
        return INSTANCE;
    }

    void applyLithiumCompat(Map<String, Option> options);

    List<MixinOverride> applyModOverrides();

    record MixinOverride(String modId, String option, boolean enabled) {

    }
}
