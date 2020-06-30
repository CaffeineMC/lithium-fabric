package me.jellysquid.mods.lithium.mixin.entity.data_tracker.optimized_entries;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(DataTracker.class)
public abstract class MixinDataTracker {
    @Mutable
    @Shadow
    @Final
    private Map<Integer, DataTracker.Entry<?>> entries;

    /**
     * Using a more specifically-optimized collection type for entity data tracker entries that provides
     * lower memory consumption and faster iterator implementation.
.     *
     * @author Maity
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void reinitializeEntries(Entity entity, CallbackInfo ci) {
        this.entries = new Int2ObjectOpenHashMap<>(this.entries);
    }
}
