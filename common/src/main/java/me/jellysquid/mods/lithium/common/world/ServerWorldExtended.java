package me.jellysquid.mods.lithium.common.world;

import net.minecraft.world.entity.Mob;

public interface ServerWorldExtended {
    void lithium$setNavigationActive(Mob mobEntity);

    void lithium$setNavigationInactive(Mob mobEntity);
}
