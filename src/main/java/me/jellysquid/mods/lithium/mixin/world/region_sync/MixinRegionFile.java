package me.jellysquid.mods.lithium.mixin.world.region_sync;

import me.jellysquid.mods.lithium.common.world.storage.SyncableStorage;
import net.minecraft.world.storage.ChunkStreamVersion;
import net.minecraft.world.storage.RegionFile;
import org.apache.commons.lang3.Validate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Path;

@Mixin(RegionFile.class)
public abstract class MixinRegionFile implements SyncableStorage {
    @Shadow
    protected abstract void writeHeader() throws IOException;

    @Shadow
    public abstract void method_26981() throws IOException;

    private boolean isBufferedDataPending;
    private boolean isBuffered;
    private boolean canUseTransactions;

    @Inject(method = "<init>(Ljava/nio/file/Path;Ljava/nio/file/Path;Lnet/minecraft/world/storage/ChunkStreamVersion;Z)V", at = @At("RETURN"))
    private void init(Path file, Path directory, ChunkStreamVersion outputChunkStreamVersion, boolean dsync, CallbackInfo ci) {
        // Implicit sync on write must be disabled to use transactions
        this.canUseTransactions = !dsync;
    }

    @Override
    public void beginTransaction() {
        this.setTransactionState(true);
    }

    @Override
    public void endTransaction() throws IOException {
        this.setTransactionState(false);

        // Do not write/sync if no data needs to be written
        if (this.isBufferedDataPending) {
            this.method_26981(); // Ensure all chunk data has been written so state is consistent with header

            this.writeHeader();
            this.method_26981(); // Wait for the header to be written

            this.isBufferedDataPending = false;
        }
    }

    private void setTransactionState(boolean state) {
        Validate.isTrue(this.canUseTransactions, "Transactions are not supported by this file");

        if (state) {
            Validate.isTrue(!this.isBuffered, "Tried to start a transaction while another is active");
        } else {
            Validate.isTrue(this.isBuffered, "Tried to end a transaction while none is active");
        }

        this.isBuffered = state;
    }

    @Redirect(method = "writeChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/RegionFile;writeHeader()V"))
    private void delayHeaderWrite(RegionFile regionFile) throws IOException {
        if (!this.isBuffered) {
            this.writeHeader();
        } else {
            this.isBufferedDataPending = true;
        }
    }
}
