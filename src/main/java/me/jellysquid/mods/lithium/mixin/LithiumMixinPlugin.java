package me.jellysquid.mods.lithium.mixin;

import me.jellysquid.mods.lithium.common.LithiumMod;
import me.jellysquid.mods.lithium.common.config.LithiumConfig;
import me.jellysquid.mods.lithium.common.config.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.File;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class LithiumMixinPlugin implements IMixinConfigPlugin {
    private static final String MIXIN_PACKAGE_ROOT = "me.jellysquid.mods.lithium.mixin.";

    private final Logger logger = LogManager.getLogger("Lithium");

    private LithiumConfig config;

    @Override
    public void onLoad(String mixinPackage) {
        try {
            this.config = LithiumConfig.load(new File("./config/lithium.properties"), "/lithium.mixins.json");
        } catch (Exception e) {
            throw new RuntimeException("Could not load configuration file for Lithium", e);
        }

        this.logger.info("Loaded configuration file for Lithium ({} options available, {} user overrides)",
                this.config.getOptionCount(), this.config.getOptionOverrideCount());
        this.logger.info("Lithium has been successfully discovered and initialized -- your game is now faster!");

        LithiumMod.CONFIG = config;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!mixinClassName.startsWith(MIXIN_PACKAGE_ROOT)) {
            return true;
        }

        String mixin = mixinClassName.substring(MIXIN_PACKAGE_ROOT.length());
        Option option = this.config.getOptionForMixin(mixin);

        if (option.isUserDefined()) {
            if (option.isEnabled()) {
                this.logger.warn("Applying mixin '{}' as user configuration forcefully enables it", mixin);
            } else {
                this.logger.warn("Not applying mixin '{}' as user configuration forcefully disables it", mixin);
            }
        }

        return option.isEnabled();
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
