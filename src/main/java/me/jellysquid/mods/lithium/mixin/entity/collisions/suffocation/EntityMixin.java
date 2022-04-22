package me.jellysquid.mods.lithium.mixin.entity.collisions.suffocation;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public abstract Vec3d getEyePos();

    @Shadow
    public World world;

    @Shadow
    public boolean noClip;

    @Shadow
    private EntityDimensions dimensions;

    /**
     * @author 2No2Name
     * @reason Avoid stream code, use optimized chunk section iteration order
     */
    @Overwrite
    public boolean isInsideWall() {
        // [VanillaCopy] The whole method functionality including bug below. Cannot use ChunkAwareBlockCollisionSweeper due to ignoring of oversized blocks
        if (this.noClip) {
            return false;
        }
        Vec3d eyePos = this.getEyePos();
        double suffocationRadius = Math.abs((double) (this.dimensions.width * 0.8f) / 2.0);

        double suffocationMinX = eyePos.x - suffocationRadius;
        double suffocationMinY = eyePos.y - 5.0E-7;
        double suffocationMinZ = eyePos.z - suffocationRadius;
        double suffocationMaxX = eyePos.x + suffocationRadius;
        double suffocationMaxY = eyePos.y + 5.0E-7;
        double suffocationMaxZ = eyePos.z + suffocationRadius;
        int minX = MathHelper.floor(suffocationMinX);
        int minY = MathHelper.floor(suffocationMinY);
        int minZ = MathHelper.floor(suffocationMinZ);
        int maxX = MathHelper.floor(suffocationMaxX);
        int maxY = MathHelper.floor(suffocationMaxY);
        int maxZ = MathHelper.floor(suffocationMaxZ);

        World world = this.world;
        //skip getting blocks when the entity is outside the world height
        //also avoids infinite loop with entities below y = Integer.MIN_VALUE (some modded servers do that)
        if (world.getBottomY() > maxY || world.getTopY() < minY) {
            return false;
        }

        BlockPos.Mutable blockPos = new BlockPos.Mutable();
        VoxelShape suffocationShape = null;

        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = minX; x <= maxX; x++) {
                    blockPos.set(x, y, z);
                    BlockState blockState = world.getBlockState(blockPos);
                    if (!blockState.isAir() && blockState.shouldSuffocate(this.world, blockPos)) {
                        if (suffocationShape == null) {
                            suffocationShape = VoxelShapes.cuboid(new Box(suffocationMinX, suffocationMinY, suffocationMinZ, suffocationMaxX, suffocationMaxY, suffocationMaxZ));
                        }
                        if (VoxelShapes.matchesAnywhere(blockState.getCollisionShape(this.world, blockPos).
                                        offset(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
                                suffocationShape, BooleanBiFunction.AND)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
