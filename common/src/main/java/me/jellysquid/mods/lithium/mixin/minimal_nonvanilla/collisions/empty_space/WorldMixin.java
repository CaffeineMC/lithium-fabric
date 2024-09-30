package me.jellysquid.mods.lithium.mixin.minimal_nonvanilla.collisions.empty_space;

import me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions;
import me.jellysquid.mods.lithium.common.shapes.VoxelShapeHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(Level.class)
public abstract class WorldMixin implements LevelAccessor {


    @Override
    public Optional<Vec3> findFreePosition(@Nullable Entity collidingEntity, VoxelShape collidingShape, Vec3 originalPosition, double maxXOffset, double maxYOffset, double maxZOffset) {
        if (collidingShape.isEmpty()) {
            return Optional.empty();
        } else {
            AABB collidingBox = collidingShape.bounds();
            AABB searchBox = collidingBox.inflate(maxXOffset, maxYOffset, maxZOffset);

            List<VoxelShape> blockCollisions = LithiumEntityCollisions.getBlockCollisions((Level) (Object) this, collidingEntity, searchBox);
            if (blockCollisions.isEmpty()) {
                return collidingShape.closestPointTo(originalPosition);
            }
            WorldBorder worldBorder = this.getWorldBorder();
            if (worldBorder != null) {
                double sideLength = Math.max(searchBox.getXsize(), searchBox.getZsize());
                double centerX = Mth.lerp(0.5, searchBox.minX, searchBox.maxX);
                double centerZ = Mth.lerp(0.5, searchBox.minZ, searchBox.maxZ);

                //Use a magic margin of 2 blocks to avoid any over-sized blocks being handled incorrectly
                boolean worldBorderIsNearby = 2 + 2 * sideLength >= worldBorder.getDistanceToBorder(centerX, centerZ);
                if (worldBorderIsNearby) {
                    blockCollisions.removeIf(voxelShape -> !worldBorder.isWithinBounds(voxelShape.bounds()));
                }
            }

            List<AABB> allCollisionBoxes = new ArrayList<>();
            for (VoxelShape blockCollision : blockCollisions) {
                for (AABB box : blockCollision.toAabbs()) {
                    //Like vanilla, fold the boxes with the entity / the max offset
                    AABB foldedBox = box.inflate(maxXOffset / 2.0, maxYOffset / 2.0, maxZOffset / 2.0);
                    allCollisionBoxes.add(foldedBox);
                }
            }
            //Get the closest point to the original position that is inside the colliding shape but not inside any of
            // the folded collision boxes, which results in the closest point where the entity can be placed.
            return VoxelShapeHelper.getClosestPointTo(originalPosition, collidingShape, allCollisionBoxes);
        }
    }
}
