package me.jellysquid.mods.lithium.common.shapes;

import me.jellysquid.mods.lithium.common.cache.EntityChunkCache;
import me.jellysquid.mods.lithium.common.entity.EntityWithChunkCache;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityContext;
import net.minecraft.util.BooleanBiFunction;
import net.minecraft.util.CuboidBlockIterator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.ViewableWorld;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class LithiumEntityCollisions {
    /**
     * [VanillaCopy] ViewableWorld#method_20812(Entity, Box)
     * This is a much, much faster implementation which uses simple collision testing against full-cube block shapes.
     * Additionally, we make use of the entity's nearby chunk cache if available to reduce the overhead of looking up
     * chunks.
     */
    public static Stream<VoxelShape> method_20812(ViewableWorld world, final Entity entity, Box entityBox) {
        EntityChunkCache cache = entity instanceof EntityWithChunkCache ? ((EntityWithChunkCache) entity).getEntityChunkCache() : null;

        int minX = MathHelper.floor(entityBox.minX - 1.0E-7D) - 1;
        int maxX = MathHelper.floor(entityBox.maxX + 1.0E-7D) + 1;
        int minY = MathHelper.floor(entityBox.minY - 1.0E-7D) - 1;
        int maxY = MathHelper.floor(entityBox.maxY + 1.0E-7D) + 1;
        int minZ = MathHelper.floor(entityBox.minZ - 1.0E-7D) - 1;
        int maxZ = MathHelper.floor(entityBox.maxZ + 1.0E-7D) + 1;

        final EntityContext context = entity == null ? EntityContext.absent() : EntityContext.of(entity);
        final CuboidBlockIterator cuboidIt = new CuboidBlockIterator(minX, minY, minZ, maxX, maxY, maxZ);
        final BlockPos.Mutable pos = new BlockPos.Mutable();
        final VoxelShape entityShape = VoxelShapes.cuboid(entityBox);

        return StreamSupport.stream(new Spliterators.AbstractSpliterator<VoxelShape>(Long.MAX_VALUE, Spliterator.NONNULL | Spliterator.IMMUTABLE) {
            boolean skipWorldBorderCheck = entity == null;

            public boolean tryAdvance(Consumer<? super VoxelShape> consumer) {
                if (!this.skipWorldBorderCheck) {
                    this.skipWorldBorderCheck = true;

                    WorldBorder border = world.getWorldBorder();

                    boolean isInsideBorder = doesEntityCollideWithWorldBorder(border, entity.getBoundingBox().contract(1.0E-7D));
                    boolean isCrossingBorder = doesEntityCollideWithWorldBorder(border, entity.getBoundingBox().expand(1.0E-7D));

                    if (!isInsideBorder && isCrossingBorder) {
                        consumer.accept(border.asVoxelShape());

                        return true;
                    }
                }

                VoxelShape shape = null;
                boolean flag;

                do {
                    int axisHit;
                    BlockState state;
                    int x, y, z;

                    do {
                        do {
                            Chunk chunk;
                            do {
                                do {
                                    if (!cuboidIt.step()) {
                                        return false;
                                    }

                                    x = cuboidIt.getX();
                                    y = cuboidIt.getY();
                                    z = cuboidIt.getZ();
                                    axisHit = cuboidIt.method_20789();
                                } while (axisHit == 3);

                                int chunkX = x >> 4;
                                int chunkZ = z >> 4;

                                if (cache != null) {
                                    chunk = cache.getChunk(chunkX, chunkZ);
                                } else {
                                    chunk = world.getChunk(chunkX, chunkZ, world.getLeastChunkStatusForCollisionCalculation(), false);
                                }
                            } while (chunk == null);

                            pos.set(x, y, z);

                            state = chunk.getBlockState(pos);
                        } while (axisHit == 1 && !state.method_17900());
                    } while (axisHit == 2 && state.getBlock() != Blocks.MOVING_PISTON);

                    VoxelShape blockShape = state.getCollisionShape(world, pos, context);
                    flag = doesEntityCollideWithShape(blockShape, entityShape, entityBox, x, y, z);

                    if (flag) {
                        shape = blockShape.offset(x, y, z);
                    }
                } while (!flag);

                if (shape == null) {
                    throw new IllegalStateException();
                }

                consumer.accept(shape);
                
                return true;
            }
        }, false);
    }

    public static boolean doesEntityCollideWithShape(VoxelShape block, VoxelShape entityShape, Box entityBox, int x, int y, int z) {
        if (block == VoxelShapes.fullCube()) {
            return entityBox.intersects(x, y, z, x + 1.0D, y + 1.0D, z + 1.0D);
        } else if (block == VoxelShapes.empty()) {
            return false;
        }

        return VoxelShapes.matchesAnywhere(block.offset(x, y, z), entityShape, BooleanBiFunction.AND);
    }

    public static boolean doesEntityCollideWithWorldBorder(WorldBorder border, Box ebox) {
        double wboxMinX = border.getCenterX() - (border.getSize() / 2);
        double wboxMinZ = border.getCenterZ() - (border.getSize() / 2);

        double wboxMaxX = border.getCenterX() + (border.getSize() / 2);
        double wboxMaxZ = border.getCenterZ() + (border.getSize() / 2);

        // Entities and world borders both use AABBs, so we can use a very simple and fast collision check
        return wboxMinX < ebox.minX && wboxMaxX > ebox.minX && wboxMinX < ebox.maxX && wboxMaxX > ebox.maxX &&
            wboxMinZ < ebox.minZ && wboxMaxZ > ebox.minZ && wboxMinZ < ebox.maxZ && wboxMaxZ > ebox.maxZ;
    }
}
