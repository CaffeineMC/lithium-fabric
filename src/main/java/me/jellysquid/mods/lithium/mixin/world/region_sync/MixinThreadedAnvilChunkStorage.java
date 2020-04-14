package me.jellysquid.mods.lithium.mixin.world.region_sync;

import me.jellysquid.mods.lithium.common.world.storage.SyncableStorage;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.chunk.Chunk;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class MixinThreadedAnvilChunkStorage {
    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    @Final
    private ServerWorld world;

    private boolean syncWrites;
    private boolean isBuffered;

    @Inject(method = "save(Z)V", at = @At("HEAD"))
    private void preChunksSaved(boolean flush, CallbackInfo ci) {
        Validate.isTrue(!this.isBuffered, "Tried to begin saving chunks before the previous transaction was complete");

        this.syncWrites = this.world.getServer().syncChunkWrites();
    }

    @Inject(method = "save(Z)V", at = @At("RETURN"))
    private void afterChunksSaved(boolean flush, CallbackInfo ci) {
        this.endTransaction();
    }

    @Inject(method = "save(Lnet/minecraft/world/chunk/Chunk;)Z", at = @At("HEAD"))
    private void preChunkSaved(Chunk chunk, CallbackInfoReturnable<Boolean> cir) {
        this.tryBeginTransaction();
    }

    private void tryBeginTransaction() {
        if (this.syncWrites && !this.isBuffered) {
            ((SyncableStorage) this).beginTransaction();

            this.isBuffered = true;
        }
    }

    /**
     * Add a write barrier after all chunks have been saved. Any given chunk will only be enqueued for writing out to
     * disk once per save tick, meaning that there is no risk of a region sector being overwritten twice. Chunks which
     * grew in size and expanded past their sector extent will be allocated at the end of the file, while chunks which
     * did not change size will simply be updated in-place.
     * <p>
     * This will ensure all data is written and synchronized to the disk before the header is written.
     */
    private void endTransaction() {
        if (this.isBuffered) {
            try {
                ((SyncableStorage) this).endTransaction();
            } catch (IOException e) {
                LOGGER.warn("Could not sync transaction", e);
            }

            this.isBuffered = false;
        }
    }
}
