package me.jellysquid.mods.lithium.common.world;

import net.minecraft.world.level.entity.EntityAccess;

public interface ChunkAwareEntityIterable<T extends EntityAccess> {
    Iterable<T> lithium$IterateEntitiesInTrackedSections();
}
