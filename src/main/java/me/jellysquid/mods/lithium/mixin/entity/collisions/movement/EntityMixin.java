package me.jellysquid.mods.lithium.mixin.entity.collisions.movement;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public abstract World getWorld();

    @Shadow
    public abstract Box getBoundingBox();

    @Redirect(
            method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getEntityCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Ljava/util/List;"
            )
    )
    private List<VoxelShape> postponeGetEntities(World world, Entity entity, Box box, @Share("requireAddEntities") LocalBooleanRef requireAddEntities) {
        requireAddEntities.set(true);
        return new ArrayList<>();
    }


    @Redirect(
            method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;adjustMovementForCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Lnet/minecraft/world/World;Ljava/util/List;)Lnet/minecraft/util/math/Vec3d;"
            )
    )
    private Vec3d collideMovementWithPostponedGetEntities(@Nullable Entity entity, Vec3d movement, Box entityBoundingBox, World world, List<VoxelShape> entityCollisions, @Share("requireAddEntities") LocalBooleanRef requireAddEntities) {
        return lithium$CollideMovement(entity, movement, entityBoundingBox, world, entityCollisions, requireAddEntities);
    }

    @ModifyVariable(
            method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",
            at = @At(
                    value = "INVOKE", shift = At.Shift.BEFORE,
                    target = "Lnet/minecraft/entity/Entity;findCollisionsForMovement(Lnet/minecraft/entity/Entity;Lnet/minecraft/world/World;Ljava/util/List;Lnet/minecraft/util/math/Box;)Ljava/util/List;")
    )
    private List<VoxelShape> collectEntities(List<VoxelShape> entityCollisions, @Share("requireAddEntities") LocalBooleanRef requireAddEntities) {
        if (requireAddEntities.get()) {
            requireAddEntities.set(false);
            ArrayList<VoxelShape> collisions = entityCollisions instanceof ArrayList<VoxelShape> ? (ArrayList<VoxelShape>) entityCollisions : new ArrayList<>(entityCollisions);
            LithiumEntityCollisions.appendEntityCollisions(collisions, this.getWorld(), (Entity) (Object) this, this.getBoundingBox());
            return collisions;
        }
        return entityCollisions;
    }

    /**
     * @author 2No2Name
     * @reason Replace with optimized implementation
     */
    @Overwrite
    public static Vec3d adjustMovementForCollisions(@Nullable Entity entity, Vec3d movement, Box entityBoundingBox, World world, List<VoxelShape> entityCollisions) {
        return lithium$CollideMovement(entity, movement, entityBoundingBox, world, entityCollisions, null);
    }

    //REFACTORED
    @Unique
    private static Vec3d lithium$CollideMovement(@Nullable Entity entity, Vec3d movement, Box entityBoundingBox, World world, List<VoxelShape> entityCollisions, LocalBooleanRef requireAddEntities) {
        double movementX = movement.x;
        double movementY = movement.y;
        double movementZ = movement.z;
        boolean isSingleAxisMovement = (movementX == 0D ? 0 : 1) + (movementY == 0D ? 0 : 1) + (movementZ == 0D ? 0 : 1) == 1;

        movementY = handleDownwardMovement(entity, movementY, entityBoundingBox, world, isSingleAxisMovement);

        Box movementSpace = isSingleAxisMovement
                ? LithiumEntityCollisions.getSmallerBoxForSingleAxisMovement(movement, entityBoundingBox, movementY, movementX, movementZ)
                : entityBoundingBox.stretch(movement);

        ChunkAwareBlockCollisionSweeper blockCollisionSweeper = new ChunkAwareBlockCollisionSweeper(world, entity, movementSpace, true);

        LazyList<VoxelShape> blockCollisions = new LazyList<>(new ArrayList<>(), blockCollisionSweeper);
        ArrayList<VoxelShape> worldBorderAndLastBlockCollision = new ArrayList<>(2);

        movementY = handleMovement(Direction.Axis.Y, movementY, entityBoundingBox, blockCollisions, entityCollisions, worldBorderAndLastBlockCollision, blockCollisionSweeper, entity, world, movementSpace, requireAddEntities);

        boolean zMovementBiggerThanXMovement = Math.abs(movementX) < Math.abs(movementZ);
        if (zMovementBiggerThanXMovement) {
            movementZ = handleMovement(Direction.Axis.Z, movementZ, entityBoundingBox, blockCollisions, entityCollisions, worldBorderAndLastBlockCollision, blockCollisionSweeper, entity, world, movementSpace, requireAddEntities);
        }

        if (movementX != 0.0) {
            movementX = handleMovement(Direction.Axis.X, movementX, entityBoundingBox, blockCollisions, entityCollisions, worldBorderAndLastBlockCollision, blockCollisionSweeper, entity, world, movementSpace, requireAddEntities);
        }

        if (!zMovementBiggerThanXMovement && movementZ != 0.0) {
            movementZ = handleMovement(Direction.Axis.Z, movementZ, entityBoundingBox, blockCollisions, entityCollisions, worldBorderAndLastBlockCollision, blockCollisionSweeper, entity, world, movementSpace, requireAddEntities);
        }

        if (requireAddEntities != null && !requireAddEntities.get()) {
            requireAddEntities.set(false);
        }

        return new Vec3d(movementX, movementY, movementZ);
    }

    private static double handleDownwardMovement(Entity entity, double movementY, Box entityBoundingBox, World world, boolean isSingleAxisMovement) {
        if (movementY < 0D) {
            VoxelShape voxelShape = LithiumEntityCollisions.getSupportingCollisionForEntity(world, entity, entityBoundingBox);
            if (voxelShape != null) {
                double v = voxelShape.calculateMaxDistance(Direction.Axis.Y, entityBoundingBox, movementY);
                if (v == 0) {
                    if (isSingleAxisMovement) {
                        return 0D;
                    }
                    movementY = 0D;
                }
            }
        }
        return movementY;
    }

    private static double handleMovement(Direction.Axis axis, double movement, Box entityBoundingBox, LazyList<VoxelShape> blockCollisions, List<VoxelShape> entityCollisions, ArrayList<VoxelShape> worldBorderAndLastBlockCollision, ChunkAwareBlockCollisionSweeper blockCollisionSweeper, Entity entity, World world, Box movementSpace, LocalBooleanRef requireAddEntities) {
        movement = VoxelShapes.calculateMaxOffset(axis, entityBoundingBox, blockCollisions, movement);
        if (movement != 0.0) {
            boolean shouldAddEntities = requireAddEntities != null && requireAddEntities.get();
            boolean shouldAddWorldBorder = true;
            boolean shouldAddLastBlock = true;

            shouldAddEntities = LithiumEntityCollisions.addEntityCollisionsIfRequired(shouldAddEntities, entity, world, entityCollisions, movementSpace);
            shouldAddWorldBorder = LithiumEntityCollisions.addWorldBorderCollisionIfRequired(shouldAddWorldBorder, entity, worldBorderAndLastBlockCollision, movementSpace);
            shouldAddLastBlock = LithiumEntityCollisions.addLastBlockCollisionIfRequired(shouldAddLastBlock, blockCollisionSweeper, worldBorderAndLastBlockCollision);

            if (!entityCollisions.isEmpty()) {
                movement = VoxelShapes.calculateMaxOffset(axis, entityBoundingBox, entityCollisions, movement);
            }
            if (!worldBorderAndLastBlockCollision.isEmpty()) {
                movement = VoxelShapes.calculateMaxOffset(axis, entityBoundingBox, worldBorderAndLastBlockCollision, movement);
            }

            if (axis == Direction.Axis.Y) {
                entityBoundingBox = entityBoundingBox.offset(0.0, movement, 0.0);
            } else if (axis == Direction.Axis.Z) {
                entityBoundingBox = entityBoundingBox.offset(0.0, 0.0, movement);
            } else if (axis == Direction.Axis.X) {
                entityBoundingBox = entityBoundingBox.offset(movement, 0.0, 0.0);
            }
        }
        return movement;
    }
}
