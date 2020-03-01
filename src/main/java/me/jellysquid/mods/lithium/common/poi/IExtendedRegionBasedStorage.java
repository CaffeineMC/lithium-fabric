package me.jellysquid.mods.lithium.common.poi;

import java.util.stream.Stream;

public interface IExtendedRegionBasedStorage<R> {
    /**
     * Fast-path for retrieving all items in a chunk column. This avoids needing to retrieve items for each sub-chunk
     * individually.
     * @param chunkX The x-coordinate of the chunk column
     * @param chunkZ The z-coordinate of the chunk column
     */
    Stream<R> getWithinChunkColumn(int chunkX, int chunkZ);
}
