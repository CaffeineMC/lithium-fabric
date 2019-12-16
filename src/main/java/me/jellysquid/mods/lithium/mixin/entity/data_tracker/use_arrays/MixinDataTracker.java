package me.jellysquid.mods.lithium.mixin.entity.data_tracker.use_arrays;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

@Mixin(DataTracker.class)
public abstract class MixinDataTracker {
    @Shadow
    @Final
    private Map<Integer, DataTracker.Entry<?>> entries;

    @Shadow
    @Final
    private ReadWriteLock lock;

    private DataTracker.Entry<?>[] entriesArray = new DataTracker.Entry<?>[0];

    /**
     * We redirect the call to add a tracked data to the internal map so we can add it to our own, faster map. The
     * map field is never mutated in-place so we can avoid expensive locking, but this might hurt for memory allocations...
     */
    @Redirect(method = "addTrackedData", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object onAddTrackedDataInsertMap(Map<Class<? extends Entity>, Integer> map, Object keyObj, Object valueObj) {
        int key = (int) keyObj;

        if (key > Byte.MAX_VALUE) {
            throw new IllegalArgumentException("Key index too large (>127)");
        }

        DataTracker.Entry<?>[] entries = this.entriesArray;

        if (entries.length <= key) {
            DataTracker.Entry<?>[] copy = new DataTracker.Entry[key + 1];

            System.arraycopy(entries, 0, copy, 0, entries.length);

            this.entriesArray = entries = copy;
        }

        entries[(byte) key] = (DataTracker.Entry<?>) valueObj;

        return this.entries.put(key, (DataTracker.Entry<?>) valueObj);
    }

    /**
     * @reason Avoid integer boxing/unboxing and locking.
     * @author JellySquid
     */
    @Overwrite
    private <T> DataTracker.Entry<T> getEntry(TrackedData<T> data) {
        this.lock.readLock().lock();

        try {
            int id = data.getId();

            if (id >= this.entriesArray.length) {
                return null;
            }

            //noinspection unchecked
            return (DataTracker.Entry<T>) this.entriesArray[id];
        } catch (Throwable cause) {
            // Move to another method so this function can be in-lined better
            throw onGetException(cause, data);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    private static <T> CrashException onGetException(Throwable cause, TrackedData<T> data) {
        CrashReport report = CrashReport.create(cause, "Getting synced entity data");

        CrashReportSection section = report.addElement("Synced entity data");
        section.add("Data ID", data);

        return new CrashException(report);
    }
}
