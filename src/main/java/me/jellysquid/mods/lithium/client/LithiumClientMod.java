package me.jellysquid.mods.lithium.client;

import me.jellysquid.mods.lithium.client.debug.CollisionTracerClient;
import me.jellysquid.mods.lithium.common.LithiumMod;
import me.jellysquid.mods.lithium.common.debug.CollisionTracer;
import net.fabricmc.api.ClientModInitializer;

public class LithiumClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (LithiumMod.CONFIG.debug.traceSweptCollisions) {
            CollisionTracer.IMPL = new CollisionTracerClient();
        }
    }
}
