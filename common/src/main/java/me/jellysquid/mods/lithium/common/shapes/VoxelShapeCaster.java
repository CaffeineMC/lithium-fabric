package me.jellysquid.mods.lithium.common.shapes;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Provides a simple interface for directly querying intersections against a shape. This can be used instead of the
 * expensive {@link net.minecraft.world.phys.shapes.Shapes#joinIsNotEmpty(VoxelShape, VoxelShape, BooleanOp)}
 * in collision detection and resolution.
 */
public interface VoxelShapeCaster {
    /**
     * Checks whether an entity's bounding box collides with this shape translated to the given coordinates.
     *
     * @param box    The entity's bounding box
     * @param blockX The x-coordinate of this shape
     * @param blockY The y-coordinate of this shape
     * @param blockZ The z-coordinate of this shape
     * @return True if the box intersects with this shape, otherwise false
     */
    boolean intersects(AABB box, double blockX, double blockY, double blockZ);
}
