package net.caffeinemc.mods.lithium.common.services;

public interface PlatformMappingInformation {
    PlatformMappingInformation INSTANCE = Services.load(PlatformMappingInformation.class);

    String mapMethodName(String fromMappings, String clazz, String method, String argsDescriptor, String mojmap);
}
