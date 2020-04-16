package me.jellysquid.mods.lithium.common.entity;

import me.jellysquid.mods.lithium.common.world.WorldHelper;
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
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static net.minecraft.predicate.entity.EntityPredicates.EXCEPT_SPECTATOR;

public class LithiumEntityCollisions {
    /**
     * [VanillaCopy] CollisionView#getBlockCollisions(Entity, Box)
     * This is a much, much faster implementation which uses simple collision testing against full-cube block shapes.
     * Checks against the world border are replaced with our own optimized functions which do not go through the
     * VoxelShape system.
     */
    public static Stream<VoxelShape> getBlockCollisions(CollisionView world, final Entity entity, Box entityBox) {
        int minX = MathHelper.floor(entityBox.x1 - 1.0E-7D) - 1;
        int maxX = MathHelper.floor(entityBox.x2 + 1.0E-7D) + 1;
        int minY = MathHelper.floor(entityBox.y1 - 1.0E-7D) - 1;
        int maxY = MathHelper.floor(entityBox.y2 + 1.0E-7D) + 1;
        int minZ = MathHelper.floor(entityBox.z1 - 1.0E-7D) - 1;
        int maxZ = MathHelper.floor(entityBox.z2 + 1.0E-7D) + 1;

        final ShapeContext context = entity == null ? ShapeContext.absent() : ShapeContext.of(entity);
        final CuboidBlockIterator cuboidIt = new CuboidBlockIterator(minX, minY, minZ, maxX, maxY, maxZ);
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
        int minX = MathHelper.floor(entityBox.x1 - 1.0E-7D) - 1;
        int maxX = MathHelper.floor(entityBox.x2 + 1.0E-7D) + 1;
        int minY = MathHelper.floor(entityBox.y1 - 1.0E-7D) - 1;
        int maxY = MathHelper.floor(entityBox.y2 + 1.0E-7D) + 1;
        int minZ = MathHelper.floor(entityBox.z1 - 1.0E-7D) - 1;
        int maxZ = MathHelper.floor(entityBox.z2 + 1.0E-7D) + 1;

        final ShapeContext context = entity == null ? ShapeContext.absent() : ShapeContext.of(entity);
        final CuboidBlockIterator cuboidIt = new CuboidBlockIterator(minX, minY, minZ, maxX, maxY, maxZ);
        final BlockPos.Mutable pos = new BlockPos.Mutable();
        final VoxelShape entityShape = VoxelShapes.cuboid(entityBox);

        if (entity != null && canEntityCollideWithWorldBorder(world, entity)) {
            return true;
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
                return true;
            }
        }

        return false;
    }

    private static boolean canInteractWithBlock(BlockState state, int edgesHit) {
        if (edgesHit == 1 && !state.exceedsCube()) {
            return false;
        }

        if (edgesHit == 2 && state.getBlock() != Blocks.MOVING_PISTON) {
            return false;
        }

        return true;
    }

    private static VoxelShape getCollidedShape(Box box, VoxelShape entityShape, VoxelShape shape, int x, int y, int z) {
        if (shape == VoxelShapes.empty()) {
            return null;
        } else if (shape == VoxelShapes.fullCube()) {
            if (!box.intersects(x, y, z, x + 1.0D, y + 1.0D, z + 1.0D)) {
                return null;
            }

            shape = shape.offset(x, y, z);
        } else {
            shape = shape.offset(x, y, z);

            if (!VoxelShapes.matchesAnywhere(shape, entityShape, BooleanBiFunction.AND)) {
                return null;
            }
        }

        return shape;
    }

    /**
     * This provides a faster check for seeing if an entity is within the world border as it avoids going through
     * the slower shape system.
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

        Box selection = box.expand(1.0E-7D);

        List<Entity> entities = view.getEntities(entity, selection);
        List<VoxelShape> shapes = new ArrayList<>();

        for (Entity otherEntity : entities) {
            if (!predicate.test(otherEntity)) {
                continue;
            }

            if (entity != null && entity.isConnectedThroughVehicle(otherEntity)) {
                continue;
            }

            Box otherEntityBox = otherEntity.getCollisionBox();

            if (otherEntityBox != null && selection.intersects(otherEntityBox)) {
                shapes.add(VoxelShapes.cuboid(otherEntityBox));
            }

            if (entity != null) {
                Box otherEntityHardBox = entity.getHardCollisionBox(otherEntity);

                if (otherEntityHardBox != null && selection.intersects(otherEntityHardBox)) {
                    shapes.add(VoxelShapes.cuboid(otherEntityHardBox));
                }
            }
        }

        return shapes;
    }

    /**
     * Partial [VanillaCopy] Classes overriding Entity.getHardCollisionBox(Entity other) or Entity.getCollisionBox()
     * The returned entity list is only used to call getCollisionBox and getHardCollisionBox. As most entities return null
     * for both of these methods, getting those is not necessary. This is why we only get entities when they overwrite
     * getCollisionBox
     * @param entityView the world
     * @param selection the box the entities have to collide with
     * @param entity the entity that is searching for the colliding entities
     * @return list of entities with collision boxes
     */
    public static List<Entity> getEntitiesWithCollisionBoxForEntity(EntityView entityView, Box selection, Entity entity) {
        if (entity != null && EntityClassGroup.HARD_COLLISION_BOX_OVERRIDE.contains(entity.getClass()) || !(entityView instanceof World)) {
            //use vanilla code when getHardCollisionBox(Entity other) is overwritten, as every entity could be relevant as argument of getHardCollisionBox
            return entityView.getEntities(entity, selection);
        } else {
            //only get entities that overwrite getCollisionBox
            return WorldHelper.getEntitiesOfClassGroup((World)entityView, entity, EntityClassGroup.COLLISION_BOX_OVERRIDE, selection, EXCEPT_SPECTATOR);
        }
    }

    /**
     * Interface to group entity types that don't always return null on getCollisionBox.
     */
    public interface CollisionBoxOverridingEntity {}
}