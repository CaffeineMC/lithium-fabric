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
import net.minecraft.world.border.WorldBorder;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class LithiumEntityCollisions {
    /**
     * [VanillaCopy] ViewableWorld#method_20812(Entity, Box)
     * This is a much, much faster implementation which uses simple collision testing against full-cube block shapes.
     * Chunk retrieval makes use of the entity's nearby chunk cache if available. Checks against the world border are
     * replaced with our own optimized functions.
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

                    WorldBorder border = world.getWorldBorder();

                    boolean isInsideBorder = LithiumEntityCollisions.isBoxFullyWithinWorldBorder(border, entity.getBoundingBox().contract(1.0E-7D));
                    boolean isCrossingBorder = LithiumEntityCollisions.isBoxFullyWithinWorldBorder(border, entity.getBoundingBox().expand(1.0E-7D));

                    if (!isInsideBorder && isCrossingBorder) {
                        consumer.accept(border.asVoxelShape());

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

    public static boolean doesEntityCollideWithShape(VoxelShape blockShape, VoxelShape entityShape, Box entityBox, int x, int y, int z) {
        if (blockShape == VoxelShapes.fullCube()) {
            return entityBox.intersects(x, y, z, x + 1.0D, y + 1.0D, z + 1.0D);
        } else if (blockShape == VoxelShapes.empty()) {
            return false;
        }

        return VoxelShapes.matchesAnywhere(blockShape.offset(x, y, z), entityShape, BooleanBiFunction.AND);
    }

    public static boolean isBoxFullyWithinWorldBorder(WorldBorder border, Box box) {
        double wboxMinX = Math.floor(border.getBoundWest());
        double wboxMinZ = Math.floor(border.getBoundNorth());

        double wboxMaxX = Math.ceil(border.getBoundEast());
        double wboxMaxZ = Math.ceil(border.getBoundSouth());
        
        return box.x1 >= wboxMinX && box.x1 < wboxMaxX && box.z1 >= wboxMinZ && box.z1 < wboxMaxZ && 
                box.x2 >= wboxMinX && box.x2 < wboxMaxX && box.z2 >= wboxMinZ && box.z2 < wboxMaxZ;
    }


}