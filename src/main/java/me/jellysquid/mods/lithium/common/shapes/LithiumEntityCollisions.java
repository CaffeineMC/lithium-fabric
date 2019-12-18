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
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;

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
    public static Stream<VoxelShape> getBlockCollisions(CollisionView world, final Entity entity, Box entityBox) {
        EntityChunkCache cache = EntityWithChunkCache.getChunkCache(entity);

        int minX = MathHelper.floor(entityBox.x1 - 1.0E-7D) - 1;
        int maxX = MathHelper.floor(entityBox.x2 + 1.0E-7D) + 1;
        int minY = MathHelper.floor(entityBox.y1 - 1.0E-7D) - 1;
        int maxY = MathHelper.floor(entityBox.y2 + 1.0E-7D) + 1;
        int minZ = MathHelper.floor(entityBox.z1 - 1.0E-7D) - 1;
        int maxZ = MathHelper.floor(entityBox.z2 + 1.0E-7D) + 1;

        final EntityContext context = entity == null ? EntityContext.absent() : EntityContext.of(entity);
        final CuboidBlockIterator cuboidIt = new CuboidBlockIterator(minX, minY, minZ, maxX, maxY, maxZ);
        final BlockPos.Mutable pos = new BlockPos.Mutable();
        final VoxelShape entityShape = VoxelShapes.cuboid(entityBox);

        return StreamSupport.stream(new Spliterators.AbstractSpliterator<VoxelShape>(Long.MAX_VALUE, Spliterator.NONNULL | Spliterator.IMMUTABLE) {
            boolean skipWorldBorderCheck = entity == null;

            public boolean tryAdvance(Consumer<? super VoxelShape> consumer) {
                if (!this.skipWorldBorderCheck) {
                    this.skipWorldBorderCheck = true;

                    VoxelShape border = world.getWorldBorder().asVoxelShape();

                    boolean isInsideBorder = VoxelShapes.matchesAnywhere(border, VoxelShapes.cuboid(entity.getBoundingBox().contract(1.0E-7D)), BooleanBiFunction.AND);
                    boolean isCrossingBorder = VoxelShapes.matchesAnywhere(border, VoxelShapes.cuboid(entity.getBoundingBox().expand(1.0E-7D)), BooleanBiFunction.AND);

                    if (!isInsideBorder && isCrossingBorder) {
                        consumer.accept(border);

                        return true;
                    }
                }

                VoxelShape shape = null;
                boolean flag;

                do {
                    int edgesHit;
                    BlockState state;
                    int x, y, z;

                    do {
                        do {
                            BlockView chunk;
                            do {
                                do {
                                    if (!cuboidIt.step()) {
                                        return false;
                                    }

                                    x = cuboidIt.getX();
                                    y = cuboidIt.getY();
                                    z = cuboidIt.getZ();
                                    edgesHit = cuboidIt.getEdgeCoordinatesCount();
                                } while (edgesHit == 3);

                                int chunkX = x >> 4;
                                int chunkZ = z >> 4;

                                if (cache != null) {
                                    chunk = cache.getChunk(chunkX, chunkZ);
                                } else {
                                    chunk = world.getExistingChunk(chunkX, chunkZ);
                                }
                            } while (chunk == null);

                            pos.set(x, y, z);

                            state = chunk.getBlockState(pos);
                        } while (edgesHit == 1 && !state.exceedsCube());
                    } while (edgesHit == 2 && state.getBlock() != Blocks.MOVING_PISTON);

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
}