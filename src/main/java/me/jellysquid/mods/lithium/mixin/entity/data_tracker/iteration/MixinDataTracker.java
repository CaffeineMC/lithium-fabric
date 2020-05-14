package me.jellysquid.mods.lithium.mixin.entity.data_tracker.iteration;

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

/**
 * Replacing the {@link DataTracker} entries map with {@link Int2ObjectOpenHashMap} will
 * reduce overall memory consumption and speed up the iteration process.
 */
@Mixin(value = DataTracker.class, priority = 999)
public class MixinDataTracker {
    @Mutable
    @Shadow
    @Final
    private Map<Integer, DataTracker.Entry<?>> entries;

    /**
     * This replaces entity data tracker entries map to a much quicker variant.
     *
     * @author Maity
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(Entity entity, CallbackInfo ci) {
        this.entries = new Int2ObjectOpenHashMap<>(this.entries);
    }
}
