package me.jellysquid.mods.lithium.mixin.region.fast_session_lock;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.SessionLockException;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class MixinThreadedAnvilChunkStorage {
    @Shadow
    @Final
    private ServerWorld world;

    private boolean hasCheckedLock;

    @Inject(method = "save(Z)V", at = @At("HEAD"))
    private void beforeSave(boolean flush, CallbackInfo ci) {
        this.hasCheckedLock = false;
    }

    @Redirect(method = "save(Lnet/minecraft/world/chunk/Chunk;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;checkSessionLock()V"))
    private void nullifySessionLock(ServerWorld serverWorld) throws SessionLockException {
        if (!this.hasCheckedLock) {
            this.world.checkSessionLock();

            this.hasCheckedLock = true;
        }
    }
}
