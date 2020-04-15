package me.jellysquid.mods.lithium.common.entity;

import me.jellysquid.mods.lithium.common.shapes.VoxelShapeExtended;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.CuboidBlockIterator;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;
import net.minecraft.world.EntityView;
import net.minecraft.world.border.WorldBorder;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class LithiumEntityCollisions {
    /**
     * [VanillaCopy] CollisionView#getBlockCollisions(Entity, Box)
     * This is a much, much faster implementation which uses simple collision testing against full-cube block shapes.
     * Checks against the world border are replaced with our own optimized functions which do not go through the
     * VoxelShape system.
     */
    public static Stream<VoxelShape> getBlockCollisions(CollisionView world, final Entity entity, Box entityBox) {
        final ShapeContext context = entity == null ? ShapeContext.absent() : ShapeContext.of(entity);
        final CuboidBlockIterator cuboidIt = createVolumeIteratorForCollision(entityBox);
        final BlockPos.Mutable pos = new BlockPos.Mutable();
        final VoxelShape entityShape = VoxelShapes.cuboid(entityBox);

        return StreamSupport.stream(new Spliterators.AbstractSpliterator<VoxelShape>(Long.MAX_VALUE, Spliterator.NONNULL | Spliterator.IMMUTABLE) {
            boolean skipWorldBorderCheck = entity == null;

            public boolean tryAdvance(Consumer<? super VoxelShape> consumer) {
                if (!this.skipWorldBorderCheck) {
                    this.skipWorldBorderCheck = true;

                    if (canEntityCollideWithWorldBorder(world, entity)) {
                        consumer.accept(world.getWorldBorder().asVoxelShape());

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

                    BlockView chunk = world.getExistingChunk(x >> 4, z >> 4);

                    if (chunk == null) {
                        continue;
                    }

                    pos.set(x, y, z);

                    BlockState state = chunk.getBlockState(pos);

                    if (!canInteractWithBlock(state, edgesHit)) {
                        continue;
                    }

                    VoxelShape blockShape = state.getCollisionShape(world, pos, context);
                    VoxelShape collidedShape = getCollidedShape(entityBox, entityShape, blockShape, x, y, z);

                    if (collidedShape != null) {
                        consumer.accept(collidedShape);
                        return true;
                    }
                }

                return false;
            }
        }, false);
    }

    private static boolean canEntityCollideWithWorldBorder(CollisionView world, Entity entity) {
        WorldBorder border = world.getWorldBorder();

        boolean isInsideBorder = LithiumEntityCollisions.isBoxFullyWithinWorldBorder(border, entity.getBoundingBox().contract(1.0E-7D));
        boolean isCrossingBorder = LithiumEntityCollisions.isBoxFullyWithinWorldBorder(border, entity.getBoundingBox().expand(1.0E-7D));

        return !isInsideBorder && isCrossingBorder;
    }

    /**
     * See {@link LithiumEntityCollisions#getBlockCollisions(CollisionView, Entity, Box)}
     *
     * @return True if the entity collided with any blocks
     */
    public static boolean doesEntityCollideWithBlocks(CollisionView world, final Entity entity, Box entityBox) {
        final ShapeContext context = entity == null ? ShapeContext.absent() : ShapeContext.of(entity);

        if (entity != null && canEntityCollideWithWorldBorder(world, entity)) {
            return true;
        }

        final CuboidBlockIterator cuboidIt = createVolumeIteratorForCollision(entityBox);
        final BlockPos.Mutable pos = new BlockPos.Mutable();
        final VoxelShape entityShape = VoxelShapes.cuboid(entityBox);

        while (cuboidIt.step()) {
            int x = cuboidIt.getX();
            int y = cuboidIt.getY();
            int z = cuboidIt.getZ();

            int edgesHit = cuboidIt.getEdgeCoordinatesCount();

            if (edgesHit == 3) {
                continue;
            }

            BlockView chunk = world.getExistingChunk(x >> 4, z >> 4);

            if (chunk == null) {
                continue;
            }

            pos.set(x, y, z);

            BlockState state = chunk.getBlockState(pos);

            if (!canInteractWithBlock(state, edgesHit)) {
                continue;
            }

            VoxelShape blockShape = state.getCollisionShape(world, pos, context);
            VoxelShape collidedShape = getCollidedShape(entityBox, entityShape, blockShape, x, y, z);

            if (collidedShape != null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns an iterator which will include every block position that can contain a collision shape which can interact
     * with the {@param box}.
     */
    private static CuboidBlockIterator createVolumeIteratorForCollision(Box box) {
        int minX = MathHelper.floor(box.x1 - 1.0E-7D) - 1;
        int maxX = MathHelper.floor(box.x2 + 1.0E-7D) + 1;
        int minY = MathHelper.floor(box.y1 - 1.0E-7D) - 1;
        int maxY = MathHelper.floor(box.y2 + 1.0E-7D) + 1;
        int minZ = MathHelper.floor(box.z1 - 1.0E-7D) - 1;
        int maxZ = MathHelper.floor(box.z2 + 1.0E-7D) + 1;

        return new CuboidBlockIterator(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * This is an artifact from vanilla which is used to avoid testing shapes in the extended portion of a volume
     * unless they are a shape which exceeds their voxel. Pistons must be special-cased here.
     *
     * @return True if the shape can be interacted with at the given edge boundary
     */
    private static boolean canInteractWithBlock(BlockState state, int edgesHit) {
        return (edgesHit != 1 || state.exceedsCube()) && (edgesHit != 2 || state.getBlock() == Blocks.MOVING_PISTON);
    }

    /**
     * Checks if the {@param entityShape} or {@param entityBox} intersects the given {@param shape} which is translated
     * to the given position. This is a very specialized implementation which tries to avoid going through VoxelShape
     * for full-cube shapes.
     *
     * @return A {@link VoxelShape} which contains the shape representing that which was collided with, otherwise null
     */
    private static VoxelShape getCollidedShape(Box entityBox, VoxelShape entityShape, VoxelShape shape, int x, int y, int z) {
        if (shape instanceof VoxelShapeExtended) {
            if (((VoxelShapeExtended) shape).intersects(entityBox, x, y, z)) {
                return shape.offset(x, y, z);
            } else {
                return null;
            }
        }

        shape = shape.offset(x, y, z);

        if (VoxelShapes.matchesAnywhere(shape, entityShape, BooleanBiFunction.AND)) {
            return shape;
        }

        return null;
    }

    /**
     * This provides a faster check for seeing if an entity is within the world border as it avoids going through
     * the slower shape system.
     *
     * @return True if the {@param box} is fully within the {@param border}, otherwise false.
     */
    public static boolean isBoxFullyWithinWorldBorder(WorldBorder border, Box box) {
        double wboxMinX = Math.floor(border.getBoundWest());
        double wboxMinZ = Math.floor(border.getBoundNorth());

        double wboxMaxX = Math.ceil(border.getBoundEast());
        double wboxMaxZ = Math.ceil(border.getBoundSouth());

        return box.x1 >= wboxMinX && box.x1 < wboxMaxX && box.z1 >= wboxMinZ && box.z1 < wboxMaxZ &&
                box.x2 >= wboxMinX && box.x2 < wboxMaxX && box.z2 >= wboxMinZ && box.z2 < wboxMaxZ;
    }

    /**
     * [VanillaCopy] EntityView#getEntityCollisions
     * Re-implements the function named above without stream code or unnecessary allocations. This can provide a small
     * boost in some situations (such as heavy entity crowding) and reduces the allocation rate significantly.
     */
    public static List<VoxelShape> getEntityCollisions(EntityView view, Entity entity, Box box, Predicate<Entity> predicate) {
        if (box.getAverageSideLength() < 1.0E-7D) {
            return Collections.emptyList();
        }

        Box selectionBox = box.expand(1.0E-7D);

        List<Entity> entities = view.getEntities(entity, selectionBox);
        List<VoxelShape> shapes = new ArrayList<>();

        for (Entity otherEntity : entities) {
            if (!predicate.test(otherEntity)) {
                continue;
            }

            if (entity != null && entity.isConnectedThroughVehicle(otherEntity)) {
                continue;
            }

            Box otherEntityBox = otherEntity.getCollisionBox();

            if (otherEntityBox != null && selectionBox.intersects(otherEntityBox)) {
                shapes.add(VoxelShapes.cuboid(otherEntityBox));
            }

            if (entity != null) {
                Box otherEntityHardBox = entity.getHardCollisionBox(otherEntity);

                if (otherEntityHardBox != null && selectionBox.intersects(otherEntityHardBox)) {
                    shapes.add(VoxelShapes.cuboid(otherEntityHardBox));
                }
            }
        }

        return shapes;
    }
}