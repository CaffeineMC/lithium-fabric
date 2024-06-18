package me.jellysquid.mods.lithium.mixin.minimal_nonvanilla.collisions.empty_space;

import me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions;
import me.jellysquid.mods.lithium.common.shapes.VoxelShapeHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.border.WorldBorder;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(World.class)
public abstract class WorldMixin implements WorldAccess {


    @Override
    public Optional<Vec3d> findClosestCollision(@Nullable Entity collidingEntity, VoxelShape collidingShape, Vec3d originalPosition, double maxXOffset, double maxYOffset, double maxZOffset) {
        if (collidingShape.isEmpty()) {
            return Optional.empty();
        } else {
            Box collidingBox = collidingShape.getBoundingBox();
            Box searchBox = collidingBox.expand(maxXOffset, maxYOffset, maxZOffset);

            List<VoxelShape> blockCollisions = LithiumEntityCollisions.getBlockCollisions((World) (Object) this, collidingEntity, searchBox);
            if (blockCollisions.isEmpty()) {
                return collidingShape.getClosestPointTo(originalPosition);
            }
            WorldBorder worldBorder = this.getWorldBorder();
            if (worldBorder != null) {
                double sideLength = Math.max(searchBox.getLengthX(), searchBox.getLengthZ());
                double centerX = MathHelper.lerp(0.5, searchBox.minX, searchBox.maxX);
                double centerZ = MathHelper.lerp(0.5, searchBox.minZ, searchBox.maxZ);

                //Use a magic margin of 2 blocks to avoid any over-sized blocks being handled incorrectly
                boolean worldBorderIsNearby = 2 + 2 * sideLength >= worldBorder.getDistanceInsideBorder(centerX, centerZ);
                if (worldBorderIsNearby) {
                    blockCollisions.removeIf(voxelShape -> !worldBorder.contains(voxelShape.getBoundingBox()));
                }
            }

            List<Box> allCollisionBoxes = new ArrayList<>();
            for (VoxelShape blockCollision : blockCollisions) {
                for (Box box : blockCollision.getBoundingBoxes()) {
                    //Like vanilla, fold the boxes with the entity / the max offset
                    Box foldedBox = box.expand(maxXOffset / 2.0, maxYOffset / 2.0, maxZOffset / 2.0);
                    allCollisionBoxes.add(foldedBox);
                }
            }
            //Get the closest point to the original position that is inside the colliding shape but not inside any of
            // the folded collision boxes, which results in the closest point where the entity can be placed.
            return VoxelShapeHelper.getClosestPointTo(originalPosition, collidingShape, allCollisionBoxes);
        }
    }
}
