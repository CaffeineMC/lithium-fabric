package me.jellysquid.mods.lithium.common.entity;

import me.jellysquid.mods.lithium.common.cache.EntityChunkCache;
import net.minecraft.entity.Entity;

public interface EntityWithChunkCache {
    /**
     * Returns the entity's chunk cache.
     *
     * @return Null if not enabled or initialized.
     */
    EntityChunkCache getEntityChunkCache();

    static EntityChunkCache getChunkCache(Entity entity) {
        return entity instanceof EntityWithChunkCache ? ((EntityWithChunkCache) entity).getEntityChunkCache() : null;
    }
}
