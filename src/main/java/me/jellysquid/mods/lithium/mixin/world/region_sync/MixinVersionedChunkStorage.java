package me.jellysquid.mods.lithium.mixin.world.region_sync;

import me.jellysquid.mods.lithium.common.world.storage.SyncableStorage;
import net.minecraft.world.storage.StorageIoWorker;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;

@Mixin(VersionedChunkStorage.class)
public class MixinVersionedChunkStorage implements SyncableStorage {
    @Shadow
    @Final
    private StorageIoWorker worker;

    @Override
    public void beginTransaction() {
        ((SyncableStorage) this.worker).beginTransaction();
    }

    @Override
    public void endTransaction() throws IOException {
        ((SyncableStorage) this.worker).endTransaction();
    }
}
