package net.caffeinemc.mods.lithium.fabric;

import net.caffeinemc.mods.lithium.common.services.PlatformMappingInformation;
import net.fabricmc.loader.api.FabricLoader;

public class FabricMappingInformation implements PlatformMappingInformation {

    @Override
    public String mapMethodName(String fromMappings, String clazz, String method, String argsDescriptor, String mojmap) {
        return FabricLoader.getInstance().getMappingResolver().mapMethodName(fromMappings, clazz, method, argsDescriptor);
    }
}
