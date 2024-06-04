package me.jellysquid.mods.lithium.mixin.entity.collisions.movement;

import me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions;
import me.jellysquid.mods.lithium.common.entity.movement.ChunkAwareBlockCollisionSweeper;
import me.jellysquid.mods.lithium.common.util.collections.LazyList;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Mixin(Entity.class)
public class EntityMixin {
    @Redirect(
            method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getEntityCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Ljava/util/List;"
            )
    )
    private List<VoxelShape> postponeGetEntities(World world, Entity entity, Box box) {
        return new ArrayList<>();
    }


    @Redirect(
            method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;adjustMovementForCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Lnet/minecraft/world/World;Ljava/util/List;)Lnet/minecraft/util/math/Vec3d;"
            )
    )
    private Vec3d collideMovementWithPostponedGetEntities(@Nullable Entity entity, Vec3d movement, Box entityBoundingBox, World world, List<VoxelShape> entityCollisions) {
        return lithium$CollideMovement(entity, movement, entityBoundingBox, world, true, entityCollisions);
    }

    /**
     * @author 2No2Name
     * @reason Replace with optimized implementation
     */
    @Overwrite
    public static Vec3d adjustMovementForCollisions(@Nullable Entity entity, Vec3d movement, Box entityBoundingBox, World world, List<VoxelShape> entityCollisions) {
        return lithium$CollideMovement(entity, movement, entityBoundingBox, world, false, entityCollisions);
    }
    @Unique
    private static Vec3d lithium$CollideMovement(@Nullable Entity entity, Vec3d movement, Box entityBoundingBox, World world, boolean shouldAddEntitiesAndWorldBorder, List<VoxelShape> otherCollisions) {
        //vanilla order: entities, world border, blocks.
        // The most important ordering constraint is that the last collision is last, since the result is not clipped to 0 when it is <1e-7.
        // Other reordering of collisions does not seem to matter.
        double movementX = movement.x;
        double movementY = movement.y;
        double movementZ = movement.z;
        boolean isSingleAxisMovement = (movementX == 0D ? 0 : 1) + (movementY == 0D ? 0 : 1) + (movementZ == 0D ? 0 : 1) == 1;
        Box movementSpace;
        if (isSingleAxisMovement) {
            if (movementY < 0D) {
                //Only downwards / gravity: Common fast path: Check block directly below center of entity first
                VoxelShape voxelShape = LithiumEntityCollisions.getCollisionShapeBelowEntity(world, entity, entityBoundingBox);
                if (voxelShape != null) {
                    double v = voxelShape.calculateMaxDistance(Direction.Axis.Y, entityBoundingBox, movementY);
                    if (v == 0) {
                        return Vec3d.ZERO;
                    }
                }
            }
            movementSpace = LithiumEntityCollisions.getSmallerBoxForSingleAxisMovement(movement, entityBoundingBox, movementY, movementX, movementZ);
        } else {
            movementSpace = entityBoundingBox.stretch(movement);
        }

        // For 1-e7 margin behavior correctness, the last block collision must be last of all collisions
        final boolean delayLastBlockCollision = shouldAddEntitiesAndWorldBorder || !otherCollisions.isEmpty();
        boolean shouldAddLastBlock = delayLastBlockCollision;
        ChunkAwareBlockCollisionSweeper blockCollisionSweeper = new ChunkAwareBlockCollisionSweeper(world, entity, movementSpace, delayLastBlockCollision);
        LazyList<VoxelShape> blockCollisions = new LazyList<>(new ArrayList<>(), blockCollisionSweeper);
        ArrayList<VoxelShape> entityCollisions = (ArrayList<VoxelShape>) otherCollisions;
        ArrayList<VoxelShape> worldBorderAndLastBlockCollision = new ArrayList<>(shouldAddEntitiesAndWorldBorder ? 2 : 1);

        if (movementY != 0.0) {
            movementY = VoxelShapes.calculateMaxOffset(Direction.Axis.Y, entityBoundingBox, blockCollisions, movementY);
            if (movementY != 0.0) {
                shouldAddEntitiesAndWorldBorder = LithiumEntityCollisions.addEntityWorldBorderCollisionIfRequired(entity, world, shouldAddEntitiesAndWorldBorder, entityCollisions, worldBorderAndLastBlockCollision, movementSpace);
                shouldAddLastBlock = LithiumEntityCollisions.addLastBlockCollisionIfRequired(shouldAddLastBlock, blockCollisionSweeper, worldBorderAndLastBlockCollision);
                if (!entityCollisions.isEmpty()) {
                    movementY = VoxelShapes.calculateMaxOffset(Direction.Axis.Y, entityBoundingBox, entityCollisions, movementY);
                }
                if (!worldBorderAndLastBlockCollision.isEmpty()) {
                    movementY = VoxelShapes.calculateMaxOffset(Direction.Axis.Y, entityBoundingBox, worldBorderAndLastBlockCollision, movementY);
                }
                
                if (movementY != 0.0) {
                    entityBoundingBox = entityBoundingBox.offset(0.0, movementY, 0.0);
                }
            }
        }
        boolean xMovementSmallerThanZMovement = Math.abs(movementX) < Math.abs(movementZ);
        if (xMovementSmallerThanZMovement) {
            movementZ = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, entityBoundingBox, blockCollisions, movementZ);
            if (movementZ != 0.0) {
                //noinspection DuplicatedCode
                shouldAddEntitiesAndWorldBorder = LithiumEntityCollisions.addEntityWorldBorderCollisionIfRequired(entity, world, shouldAddEntitiesAndWorldBorder, entityCollisions, worldBorderAndLastBlockCollision, movementSpace);
                shouldAddLastBlock = LithiumEntityCollisions.addLastBlockCollisionIfRequired(shouldAddLastBlock, blockCollisionSweeper, worldBorderAndLastBlockCollision);
                if (!entityCollisions.isEmpty()) {
                    movementZ = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, entityBoundingBox, entityCollisions, movementZ);
                }
                if (!worldBorderAndLastBlockCollision.isEmpty()) {
                    movementZ = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, entityBoundingBox, worldBorderAndLastBlockCollision, movementZ);
                }

                if (movementZ != 0.0) {
                    entityBoundingBox = entityBoundingBox.offset(0.0, 0.0, movementZ);
                }
            }
        }
        if (movementX != 0.0) {
            movementX = VoxelShapes.calculateMaxOffset(Direction.Axis.X, entityBoundingBox, blockCollisions, movementX);
            if (movementX != 0.0) {
                shouldAddEntitiesAndWorldBorder = LithiumEntityCollisions.addEntityWorldBorderCollisionIfRequired(entity, world, shouldAddEntitiesAndWorldBorder, entityCollisions, worldBorderAndLastBlockCollision, movementSpace);
                shouldAddLastBlock = LithiumEntityCollisions.addLastBlockCollisionIfRequired(shouldAddLastBlock, blockCollisionSweeper, worldBorderAndLastBlockCollision);
                if (!entityCollisions.isEmpty()) {
                    movementX = VoxelShapes.calculateMaxOffset(Direction.Axis.X, entityBoundingBox, entityCollisions, movementX);
                }
                if (!worldBorderAndLastBlockCollision.isEmpty()) {
                    movementX = VoxelShapes.calculateMaxOffset(Direction.Axis.X, entityBoundingBox, worldBorderAndLastBlockCollision, movementX);
                }

                if (movementX != 0.0) {
                    entityBoundingBox = entityBoundingBox.offset(movementX, 0.0, 0.0);
                }
            }
        }
        if (!xMovementSmallerThanZMovement && movementZ != 0.0) {
            movementZ = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, entityBoundingBox, blockCollisions, movementZ);
            if (movementZ != 0.0) {
                //noinspection UnusedAssignment,DuplicatedCode
                shouldAddEntitiesAndWorldBorder = LithiumEntityCollisions.addEntityWorldBorderCollisionIfRequired(entity, world, shouldAddEntitiesAndWorldBorder, entityCollisions, worldBorderAndLastBlockCollision, movementSpace);
                //noinspection UnusedAssignment
                shouldAddLastBlock = LithiumEntityCollisions.addLastBlockCollisionIfRequired(shouldAddLastBlock, blockCollisionSweeper, worldBorderAndLastBlockCollision);
                if (!entityCollisions.isEmpty()) {
                    movementZ = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, entityBoundingBox, entityCollisions, movementZ);
                }
                if (!worldBorderAndLastBlockCollision.isEmpty()) {
                    movementZ = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, entityBoundingBox, worldBorderAndLastBlockCollision, movementZ);
                }
                //No need to offset box here, as it is the last axis
            }
        }
        return new Vec3d(movementX, movementY, movementZ);
    }
}
