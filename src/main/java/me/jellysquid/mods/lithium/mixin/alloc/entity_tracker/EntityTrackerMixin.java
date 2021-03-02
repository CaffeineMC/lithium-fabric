package me.jellysquid.mods.lithium.mixin.alloc.entity_tracker;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.EntityTrackingListener;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ThreadedAnvilChunkStorage.EntityTracker.class)
public class EntityTrackerMixin {
    @Mutable
    @Shadow
    @Final
    private Set<EntityTrackingListener> listeners;


    @Inject(method = "<init>", at = @At("RETURN"))
    private void reinit(ThreadedAnvilChunkStorage parent /* non-static class parent */, Entity entity, int maxDistance,
                        int tickInterval, boolean alwaysUpdateVelocity, CallbackInfo ci) {
        // Uses less memory, and will cache the returned iterator
        this.listeners = new ObjectOpenHashSet<>(this.listeners);
    }
}
