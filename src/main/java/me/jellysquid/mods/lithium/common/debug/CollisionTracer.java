package me.jellysquid.mods.lithium.common.debug;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public abstract class CollisionTracer {
    /**
     * The current implementation of the tracer. By default, this is initialized to a no-op implementation and calls to it
     * in hot spots are eliminated.
     */
    public static CollisionTracer IMPL = new NoOp();

    /**
     * Marks a block that was tested for collision.
     */
    public abstract void addTouchedBlock(int x, int y, int z);

    /**
     * Marks a ray which was cast through blocks for collision.
     */
    public abstract void addTracedRay(double x1, double y1, double z1, double x2, double y2, double z2);

    /**
     * Renders the visuals of this tracer in the current world.
     */
    @Environment(EnvType.CLIENT)
    public abstract void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ);

    /**
     * Called once every frame to tick the visuals.
     */
    public abstract void tick();

    /**
     * Returns whether or not this tracer should be rendered in the world.
     */
    public boolean isVisible() {
        return false;
    }

    /**
     * The default no-operation implementation. Smart JVMs will eliminate calls to this.
     */
    private static class NoOp extends CollisionTracer {
        @Override
        public void addTouchedBlock(int x, int y, int z) {

        }

        @Override
        public void addTracedRay(double x1, double y1, double z1, double x2, double y2, double z2) {

        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {

        }

        @Override
        public void tick() {

        }
    }

}
