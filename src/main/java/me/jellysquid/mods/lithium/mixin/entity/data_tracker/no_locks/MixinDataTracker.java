package me.jellysquid.mods.lithium.mixin.entity.data_tracker.no_locks;

import net.minecraft.entity.data.DataTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.locks.Lock;

/**
 * The vanilla implementation of {@link DataTracker} performs locking when fetching or updating data due to a legacy
 * quirk in older versions of the game where updates would occur on a network thread for (de)serialization while entities
 * were ticking and accessing values from it on the main thread. In newer versions (1.14+) this no longer happens.
 *
 * The DataTracker is expected to only ever updated on the main-thread (or the thread owning it in recent versions when
 * baking entities) during entity initialization and main-thread network updates, and as such the locking mechanism
 * is unnecessary since the job is to only protect against simultaneous reading and writing.
 */
@Mixin(value = DataTracker.class, priority = 1001)
public abstract class MixinDataTracker {
    @Redirect(method = {
            "getEntry", "addTrackedData", "getDirtyEntries", "getAllEntries", "writeUpdatedEntries", "clearDirty"
    }, at = @At(value = "INVOKE", target = "Ljava/util/concurrent/locks/Lock;lock()V"))
    private void nullifyLock(Lock lock) {

    }

    @Redirect(method = {
            "getEntry", "addTrackedData", "getDirtyEntries", "getAllEntries", "writeUpdatedEntries", "clearDirty"
    }, at = @At(value = "INVOKE", target = "Ljava/util/concurrent/locks/Lock;unlock()V"))
    private void nullifyUnlock(Lock lock) {

    }
}
