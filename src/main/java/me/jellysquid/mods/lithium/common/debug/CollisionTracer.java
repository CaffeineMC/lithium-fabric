package me.jellysquid.mods.lithium.common.debug;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public abstract class CollisionTracer {
    public static CollisionTracer IMPL = new NoOp();

    public abstract void addTouchedBlock(int x, int y, int z);

    public abstract void addTracedRay(double x1, double y1, double z1, double x2, double y2, double z2);

    @Environment(EnvType.CLIENT)
    public abstract void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ);

    public abstract void tick();

    public boolean isVisible() {
        return false;
    }

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
