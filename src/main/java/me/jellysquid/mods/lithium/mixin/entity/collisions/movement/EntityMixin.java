package me.jellysquid.mods.lithium.mixin.entity.collisions.movement;

import me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collections;
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
    private List<VoxelShape> getEmptyList(World world, Entity entity, Box box) {
        return Collections.emptyList();
    }

    /**
     * @author 2No2Name
     * @reason Replace with optimized implementation
     */
    @Overwrite
    public static Vec3d adjustMovementForCollisions(@Nullable Entity entity, Vec3d movement, Box entityBoundingBox, World world, List<VoxelShape> collisions) {
        return lithiumCollideMultiAxisMovement(entity, movement, entityBoundingBox, world);
    }

    private static Vec3d lithiumCollideMultiAxisMovement(@Nullable Entity entity, Vec3d movement, Box entityBoundingBox, World world) {
        //vanilla order: entities, worldborder, blocks. It is unknown whether changing this order changes the result regarding the confusing 1e-7 VoxelShape margin behavior. Not yet investigated
        double velX = movement.x;
        double velY = movement.y;
        double velZ = movement.z;
        boolean isVerticalOnly = velX == 0 && velZ == 0;
        Box movementSpace;
        if (isVerticalOnly) {
            if (velY < 0) {
                movementSpace = new Box(entityBoundingBox.minX, entityBoundingBox.minY + velY, entityBoundingBox.minZ, entityBoundingBox.maxX, entityBoundingBox.minY, entityBoundingBox.maxZ);
            } else {
                movementSpace = new Box(entityBoundingBox.minX, entityBoundingBox.maxY, entityBoundingBox.minZ, entityBoundingBox.maxX, entityBoundingBox.maxY + velY, entityBoundingBox.maxZ);
            }
        } else {
            movementSpace = entityBoundingBox.stretch(movement);
        }

        List<VoxelShape> blockCollisions = LithiumEntityCollisions.getBlockCollisions(world, entity, movementSpace);
        List<VoxelShape> entityWorldBorderCollisions = null;

        if (velY != 0.0) {
            velY = VoxelShapes.calculateMaxOffset(Direction.Axis.Y, entityBoundingBox, blockCollisions, velY);
            if (velY != 0.0) {
                entityWorldBorderCollisions = LithiumEntityCollisions.getEntityWorldBorderCollisions(world, entity, movementSpace, entity != null);
                velY = VoxelShapes.calculateMaxOffset(Direction.Axis.Y, entityBoundingBox, entityWorldBorderCollisions, velY);
                if (velY != 0.0) {
                    entityBoundingBox = entityBoundingBox.offset(0.0, velY, 0.0);
                }
            }
        }
        boolean velXSmallerVelZ = Math.abs(velX) < Math.abs(velZ);
        if (velXSmallerVelZ) {
            velZ = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, entityBoundingBox, blockCollisions, velZ);
            if (velZ != 0.0) {
                if (entityWorldBorderCollisions == null) {
                    entityWorldBorderCollisions = LithiumEntityCollisions.getEntityWorldBorderCollisions(world, entity, movementSpace, entity != null);
                }

                velZ = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, entityBoundingBox, entityWorldBorderCollisions, velZ);
                if (velZ != 0.0) {
                    entityBoundingBox = entityBoundingBox.offset(0.0, 0.0, velZ);
                }
            }
        }
        if (velX != 0.0) {
            velX = VoxelShapes.calculateMaxOffset(Direction.Axis.X, entityBoundingBox, blockCollisions, velX);
            if (velX != 0.0) {
                if (entityWorldBorderCollisions == null) {
                    entityWorldBorderCollisions = LithiumEntityCollisions.getEntityWorldBorderCollisions(world, entity, movementSpace, entity != null);
                }

                velX = VoxelShapes.calculateMaxOffset(Direction.Axis.X, entityBoundingBox, entityWorldBorderCollisions, velX);
                if (velX != 0.0) {
                    entityBoundingBox = entityBoundingBox.offset(velX, 0.0, 0.0);
                }
            }
        }
        if (!velXSmallerVelZ && velZ != 0.0) {
            velZ = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, entityBoundingBox, blockCollisions, velZ);
            if (velZ != 0.0) {
                if (entityWorldBorderCollisions == null) {
                    entityWorldBorderCollisions = LithiumEntityCollisions.getEntityWorldBorderCollisions(world, entity, movementSpace, entity != null);
                }

                velZ = VoxelShapes.calculateMaxOffset(Direction.Axis.Z, entityBoundingBox, entityWorldBorderCollisions, velZ);
            }
        }
        return new Vec3d(velX, velY, velZ);
    }
}
