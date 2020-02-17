package me.jellysquid.mods.lithium.mixin.client.tracers;

import me.jellysquid.mods.lithium.common.debug.CollisionTracer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Installs a hook to the {@link DebugRenderer} which allows for our tracers to be rendered.
 */
@Mixin(DebugRenderer.class)
public class MixinDebugRenderer {
    @Inject(method = "render", at = @At("HEAD"))
    private void renderHook(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        CollisionTracer tracer = CollisionTracer.IMPL;
        tracer.tick();

        if (tracer.isVisible()) {
            tracer.render(matrices, vertexConsumers, cameraX, cameraY, cameraZ);
        }
    }
}
