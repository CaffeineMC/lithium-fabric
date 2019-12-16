package me.jellysquid.mods.lithium.mixin.entity.data_tracker.no_locks;

import net.minecraft.entity.data.DataTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.locks.Lock;

@Mixin(value = DataTracker.class, priority = 1001)
public abstract class MixinDataTracker {
    @Redirect(method = {
            "getEntry", "addTrackedData", "getDirtyEntries", "toPacketByteBuf", "getAllEntries", "writeUpdatedEntries", "clearDirty"
    }, at = @At(value = "INVOKE", target = "Ljava/util/concurrent/locks/Lock;lock()V"))
    private void nullifyLock(Lock lock) {

    }

    @Redirect(method = {
            "getEntry", "addTrackedData", "getDirtyEntries", "toPacketByteBuf", "getAllEntries", "writeUpdatedEntries", "clearDirty"
    }, at = @At(value = "INVOKE", target = "Ljava/util/concurrent/locks/Lock;unlock()V"))
    private void nullifyUnlock(Lock lock) {

    }
}
