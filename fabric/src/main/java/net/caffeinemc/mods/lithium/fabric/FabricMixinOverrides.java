package net.caffeinemc.mods.lithium.fabric;

import net.caffeinemc.mods.lithium.common.config.Option;
import net.caffeinemc.mods.lithium.common.services.PlatformMixinOverrides;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FabricMixinOverrides implements PlatformMixinOverrides {
    protected static final String JSON_KEY_LITHIUM_OPTIONS = "lithium:options";

    @Override
    public void applyLithiumCompat(Map<String, Option> options) {
        if (FabricLoader.getInstance().isModLoaded("worldedit")) {
            options.get("mixin.fabric.compat.worldedit").addModOverride(true, "lithium");
        }
        if (FabricLoader.getInstance().isModLoaded("fabric-transfer-api-v1")) {
            options.get("mixin.fabric.compat.transfer_api").addModOverride(true, "lithium");
        }
    }

    @Override
    public List<PlatformMixinOverrides.MixinOverride> applyModOverrides() {
        List<MixinOverride> list = new ArrayList<>();

        for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
            ModMetadata meta = container.getMetadata();

            if (meta.containsCustomValue(JSON_KEY_LITHIUM_OPTIONS)) {
                CustomValue overrides = meta.getCustomValue(JSON_KEY_LITHIUM_OPTIONS);

                if (overrides.getType() != CustomValue.CvType.OBJECT) {
                    System.out.printf("[Lithium] Mod '%s' contains invalid Lithium option overrides, ignoring", meta.getId());
                    continue;
                }

                for (Map.Entry<String, CustomValue> entry : overrides.getAsObject()) {
                    if (entry.getValue().getType() != CustomValue.CvType.BOOLEAN) {
                        System.out.printf("[Lithium] Mod '%s' attempted to override option '%s' with an invalid value, ignoring", meta.getId(), entry.getKey());
                        continue;
                    }

                    list.add(new MixinOverride(meta.getId(), entry.getKey(), entry.getValue().getAsBoolean()));
                }
            }
        }

        return list;
    }
}
