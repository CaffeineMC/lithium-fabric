package me.jellysquid.mods.lithium.common.client;

import net.minecraft.entity.Entity;
import net.minecraft.world.entity.ClientEntityManager;

public interface ClientWorldAccessor {
    ClientEntityManager<Entity> lithium$getEntityManager();
}
