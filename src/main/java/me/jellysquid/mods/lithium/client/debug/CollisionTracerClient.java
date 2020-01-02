package me.jellysquid.mods.lithium.client.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.jellysquid.mods.lithium.common.debug.CollisionTracer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.StampedLock;

public class CollisionTracerClient extends CollisionTracer {
    // This *WILL* be accessed from multiple threads and we want to be able to visualize it as such.
    private final StampedLock lock = new StampedLock();

    private final Long2ObjectOpenHashMap<TracedBlock> blocks = new Long2ObjectOpenHashMap<>();

    private final List<TracedRay> rays = new ArrayList<>();

    @Override
    public void addTouchedBlock(int x, int y, int z) {
        long l = this.lock.writeLock();

        try {
            final long pos = BlockPos.asLong(x, y, z);
            final TracedBlock block = this.blocks.get(pos);

            if (block == null) {
                this.blocks.put(pos, new TracedBlock(new BlockPos(x, y, z)));

                return;
            }

            block.ticks = 0;
        } finally {
            this.lock.unlockWrite(l);
        }
    }

    @Override
    public void addTracedRay(double x1, double y1, double z1, double x2, double y2, double z2) {
        long l = this.lock.writeLock();

        try {
            this.rays.add(new TracedRay(new Vec3d(x1, y1, z1), new Vec3d(x2, y2, z2)));
        } finally {
            this.lock.unlockWrite(l);
        }
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        RenderSystem.pushMatrix();

        long l = this.lock.readLock();

        try {
            this.renderTracedRays(matrices, vertexConsumers, cameraX, cameraY, cameraZ);
            this.renderTracedBlocks(matrices, vertexConsumers, cameraX, cameraY, cameraZ);
        } finally {
            this.lock.unlockRead(l);
        }

        RenderSystem.popMatrix();
    }

    private void renderTracedRays(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());

        Matrix4f matrix4f = matrices.peek().getModel();

        for (TracedRay ray : this.rays) {
            float x1 = (float) (ray.start.x + -cameraX);
            float y1 = (float) (ray.start.y + -cameraY);
            float z1 = (float) (ray.start.z + -cameraZ);

            float x2 = (float) (ray.end.x + -cameraX);
            float y2 = (float) (ray.end.y + -cameraY);
            float z2 = (float) (ray.end.z + -cameraZ);

            float alpha = ray.getProgress();

            vertexConsumer.vertex(matrix4f, x1, y1, z1).color(1.0F, 0.0F, 0.0F, alpha).next();
            vertexConsumer.vertex(matrix4f, x2, y2, z2).color(1.0F, 0.0F, 0.0F, alpha).next();
        }
    }

    private void renderTracedBlocks(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());

        for (TracedBlock block : this.blocks.values()) {
            BlockPos pos = block.pos;

            double x = pos.getX() + -cameraX;
            double y = pos.getY() + -cameraY;
            double z = pos.getZ() + -cameraZ;

            WorldRenderer.drawBox(matrices, vertexConsumer, x, y, z, x + 1, y + 1, z + 1, 0.0F, 0.0F, 0.0F, block.getProgress());
        }
    }

    @Override
    public void tick() {
        long l = this.lock.writeLock();

        try {
            this.blocks.values().removeIf(TracedBlock::tick);
            this.rays.removeIf(TracedRay::tick);
        } finally {
            this.lock.unlockWrite(l);
        }
    }

    private static class TracedBlock extends TracedObject {
        public final BlockPos pos;

        private TracedBlock(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public float getProgress() {
            return super.getProgress() * 0.5f;
        }
    }

    private static class TracedRay extends TracedObject {
        public final Vec3d start, end;

        private TracedRay(Vec3d start, Vec3d end) {
            this.start = start;
            this.end = end;
        }
    }

    private static class TracedObject {
        public final int duration = 30;
        public int ticks;

        public boolean tick() {
            this.ticks++;

            return this.ticks >= this.duration;
        }

        public float getProgress() {
            return 1.0F - (this.ticks / (float) this.duration);
        }
    }
}
