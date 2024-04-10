package me.jellysquid.mods.lithium.common.entity;

import net.minecraft.entity.ai.pathing.EntityNavigation;

public interface NavigatingEntity {
    boolean lithium$isRegisteredToWorld();

    void lithium$setRegisteredToWorld(EntityNavigation navigation);

    EntityNavigation lithium$getRegisteredNavigation();

    void lithium$updateNavigationRegistration();

}
