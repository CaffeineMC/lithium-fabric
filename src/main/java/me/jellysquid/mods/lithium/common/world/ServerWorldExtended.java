package me.jellysquid.mods.lithium.common.world;

import net.minecraft.entity.mob.MobEntity;

public interface ServerWorldExtended {
    void lithium$setNavigationActive(MobEntity mobEntity);

    void lithium$setNavigationInactive(MobEntity mobEntity);
}
