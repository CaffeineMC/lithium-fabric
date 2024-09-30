package me.jellysquid.mods.lithium.common.entity;

import net.minecraft.world.entity.ai.navigation.PathNavigation;

public interface NavigatingEntity {
    boolean lithium$isRegisteredToWorld();

    void lithium$setRegisteredToWorld(PathNavigation navigation);

    PathNavigation lithium$getRegisteredNavigation();

    void lithium$updateNavigationRegistration();

}
