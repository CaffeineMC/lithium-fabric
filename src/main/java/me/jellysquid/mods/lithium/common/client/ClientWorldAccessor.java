package me.jellysquid.mods.lithium.common.client;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.TransientEntitySectionManager;

public interface ClientWorldAccessor {
    TransientEntitySectionManager<Entity> lithium$getEntityManager();
}
