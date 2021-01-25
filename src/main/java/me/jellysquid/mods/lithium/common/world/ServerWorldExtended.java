package me.jellysquid.mods.lithium.common.world;

import net.minecraft.entity.mob.MobEntity;

public interface ServerWorldExtended {
    void setNavigationActive(MobEntity entityNavigation);
    void setNavigationInactive(MobEntity entityNavigation);
}
