package me.jellysquid.mods.lithium.mixin.world.region_sync;

import me.jellysquid.mods.lithium.common.world.storage.SyncableStorage;
import net.minecraft.world.storage.RegionBasedStorage;
import net.minecraft.world.storage.StorageIoWorker;
import org.apache.commons.lang3.Validate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Mixin(StorageIoWorker.class)
public abstract class MixinStorageIoWorker implements SyncableStorage {
    @Shadow
    protected abstract <T> CompletableFuture<T> run(Function<CompletableFuture<T>, Runnable> taskFactory);

    @Shadow
    @Final
    private RegionBasedStorage storage;

    private boolean isBuffered = false;

    @Override
    public void beginTransaction() {
        Validate.isTrue(!this.isBuffered, "Tried to start a transaction while another is open");

        this.isBuffered = true;
        this.run((completableFuture) -> () -> {
            ((SyncableStorage) (Object) this.storage).beginTransaction();
        });
    }

    @Override
    public void endTransaction() {
        Validate.isTrue(this.isBuffered, "Tried to end a transaction when none is active");

        this.isBuffered = false;
        this.run((completableFuture) -> () -> {
            try {
                ((SyncableStorage) (Object) this.storage).endTransaction();
            } catch (IOException e) {
                throw new RuntimeException("Could not sync changes", e);
            }
        });
    }
}
