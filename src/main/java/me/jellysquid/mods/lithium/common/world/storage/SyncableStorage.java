package me.jellysquid.mods.lithium.common.world.storage;

import java.io.IOException;

public interface SyncableStorage {
    /**
     * Begins a transaction. Changes will not be committed until {@link SyncableStorage#endTransaction()} is called.
     */
    void beginTransaction();

    /**
     * Ends a transaction, committing changes made after calling {@link SyncableStorage#beginTransaction()}. This adds a
     * write barrier that will block all queued operations after it until a full sync with the file system has occurred.
     */
    void endTransaction() throws IOException;
}
