package me.jellysquid.mods.lithium.mixin.world.region_sync;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerWorld.class)
public class MixinServerWorld {
    // Lambda in ServerWorld#<init>
    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(method = "method_14168", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;syncChunkWrites()Z"))
    private static boolean redirectUseSyncWrites(MinecraftServer server) {
        // Disable implicit sync-on-write so we can use transactions ourselves
        return false;
    }
}
