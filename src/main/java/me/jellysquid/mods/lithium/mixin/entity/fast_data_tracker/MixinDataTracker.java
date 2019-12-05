package me.jellysquid.mods.lithium.mixin.entity.fast_data_tracker;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
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

@Mixin(DataTracker.class)
public abstract class MixinDataTracker {
    @Shadow
    @Final
    private Map<Integer, DataTracker.Entry<?>> entries;

    // Immutable! Updates must replace the variable. TODO: Replace with immutable map type
    private volatile Int2ObjectOpenHashMap<DataTracker.Entry<?>> entriesVolatile = new Int2ObjectOpenHashMap<>();

    /**
     * We redirect the call to add a tracked data to the internal map so we can add it to our own, faster map. The
     * map field is never mutated in-place so we can avoid expensive locking, but this might hurt for memory allocations...
     */
    @Redirect(method = "addTrackedData", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object onAddTrackedDataInsertMap(Map<Class<? extends Entity>, Integer> map, Object key, Object value) {
        Int2ObjectOpenHashMap<DataTracker.Entry<?>> copy = this.entriesVolatile.clone();
        copy.put((int) key, (DataTracker.Entry<?>) value);

        this.entriesVolatile = copy;

        return this.entries.put((Integer) key, (DataTracker.Entry<?>) value);
    }

    /**
     * @reason Avoid integer boxing/unboxing and locking.
     * @author JellySquid
     */
    @Overwrite
    private <T> DataTracker.Entry<T> getEntry(TrackedData<T> data) {
        try {
            //noinspection unchecked
            return (DataTracker.Entry<T>) this.entriesVolatile.get(data.getId());
        } catch (Throwable cause) {
            // Move to another method so this function can be in-lined better
            throw onGetException(cause, data);
        }
    }

    private static <T> CrashException onGetException(Throwable cause, TrackedData<T> data) {
        CrashReport report = CrashReport.create(cause, "Getting synced entity data");

        CrashReportSection section = report.addElement("Synced entity data");
        section.add("Data ID", data);

        return new CrashException(report);
    }
}
