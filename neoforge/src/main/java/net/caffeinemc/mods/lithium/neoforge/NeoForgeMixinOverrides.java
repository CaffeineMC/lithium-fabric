package net.caffeinemc.mods.lithium.neoforge;

import net.caffeinemc.mods.lithium.common.config.Option;
import net.caffeinemc.mods.lithium.common.services.PlatformMixinOverrides;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.moddiscovery.ModInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NeoForgeMixinOverrides implements PlatformMixinOverrides {
    protected static final String JSON_KEY_LITHIUM_OPTIONS = "lithium:options";

    @Override
    public void applyLithiumCompat(Map<String, Option> options) {

    }
    @Override
    public List<PlatformMixinOverrides.MixinOverride> applyModOverrides() {
        List<MixinOverride> list = new ArrayList<>();

        for (ModInfo meta : FMLLoader.getLoadingModList().getMods()) {
            meta.getConfigElement(JSON_KEY_LITHIUM_OPTIONS).ifPresent(override -> {
                if (override instanceof Map<?, ?> overrides && overrides.keySet().stream().allMatch(key -> key instanceof String)) {
                    overrides.forEach((key, value) -> {
                        if (!(value instanceof Boolean) || !(key instanceof String)) {
                            System.out.printf("[Lithium] Mod '%s' attempted to override option '%s' with an invalid value, ignoring", meta.getModId(), key);
                            return;
                        }

                        list.add(new MixinOverride(meta.getModId(), (String) key, (Boolean) value));
                    });
                } else {
                    System.out.printf("[Lithium] '%s' contains invalid Lithium option overrides, ignoring", meta.getModId());
                }
            });
        }

        return list;
    }
}
