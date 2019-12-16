package me.jellysquid.mods.lithium.common.entity;

import me.jellysquid.mods.lithium.common.cache.EntityChunkCache;

public interface EntityWithChunkCache {
    /**
     * Returns the entity's chunk cache.
     *
     * @return Null if not enabled or initialized.
     */
    EntityChunkCache getEntityChunkCache();
}
