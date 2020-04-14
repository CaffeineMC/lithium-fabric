package me.jellysquid.mods.lithium.mixin.world.region_sync;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import me.jellysquid.mods.lithium.common.world.storage.SyncableStorage;
import net.minecraft.world.storage.RegionBasedStorage;
import net.minecraft.world.storage.RegionFile;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;

@Mixin(RegionBasedStorage.class)
public class MixinRegionBasedStorage implements SyncableStorage {
    @Shadow
    @Final
    private Long2ObjectLinkedOpenHashMap<RegionFile> cachedRegionFiles;

    @Override
    public void beginTransaction() {
        for (RegionFile file : this.cachedRegionFiles.values()) {
            ((SyncableStorage) file).beginTransaction();
        }
    }

    @Override
    public void endTransaction() throws IOException {
        for (RegionFile file : this.cachedRegionFiles.values()) {
            ((SyncableStorage) file).endTransaction();
        }
    }
}
