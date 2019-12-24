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

                while (cuboidIt.step()) {
                    int x = cuboidIt.getX();
                    int y = cuboidIt.getY();
                    int z = cuboidIt.getZ();

                    int edgesHit = cuboidIt.getEdgeCoordinatesCount();

                    if (edgesHit == 3) {
                        continue;
                    }

                    BlockView chunk;

                    if (cache != null) {
                        chunk = cache.getChunk(x >> 4, z >> 4);
                    } else {
                        chunk = world.getExistingChunk(x >> 4, z >> 4);
                    }

                    if (chunk == null) {
                        continue;
                    }

                    pos.set(x, y, z);

                    BlockState state = chunk.getBlockState(pos);

                    if (edgesHit == 1 && !state.exceedsCube()) {
                        continue;
                    }

                    if (edgesHit == 2 && state.getBlock() == Blocks.MOVING_PISTON) {
                        continue;
                    }

                    VoxelShape blockShape = state.getCollisionShape(world, pos, context);

                    if (blockShape == VoxelShapes.empty()) {
                        continue;
                    }

                    if (blockShape == VoxelShapes.fullCube()) {
                        if (entityBox.intersects(x, y, z, x + 1.0D, y + 1.0D, z + 1.0D)) {
                            consumer.accept(blockShape.offset(x, y, z));

                            return true;
                        }
                    } else {
                        VoxelShape shape = blockShape.offset(x, y, z);

                        if (VoxelShapes.matchesAnywhere(shape, entityShape, BooleanBiFunction.AND)) {
                            consumer.accept(shape);

                            return true;
                        }
                    }
                }

                return false;
            }
        }, false);
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