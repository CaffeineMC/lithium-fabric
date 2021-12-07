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
        // [VanillaCopy] The whole method functionality including bug below. Cannot use ChunkAwareBlockCollisionSweeper due to special bug behavior and ignoring of oversized blocks
        if (this.noClip) {
            return false;
        }
        Vec3d vec3d = this.getEyePos();
        double suffocationRadius = Math.abs((double) (this.dimensions.width * 0.8f) / 2.0);

        BlockPos incorrectSuffocationPos = new BlockPos(vec3d);

        int minX = MathHelper.floor(vec3d.x - suffocationRadius);
        int minY = MathHelper.floor(vec3d.y - 5.0E-7);
        int minZ = MathHelper.floor(vec3d.z - suffocationRadius);
        int maxX = MathHelper.floor(vec3d.x + suffocationRadius);
        int maxY = MathHelper.floor(vec3d.y + 5.0E-7);
        int maxZ = MathHelper.floor(vec3d.z + suffocationRadius);

        World world = this.world;
        BlockPos.Mutable blockPos = new BlockPos.Mutable();
        VoxelShape suffocationShape = null;

        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = minX; x <= maxX; x++) {
                    blockPos.set(x, y, z);
                    BlockState blockState = world.getBlockState(blockPos);
                    // [VanillaCopy] https://bugs.mojang.com/browse/MC-242543 (incorrectly marked as cannot reproduce)
                    if (!blockState.isAir() && blockState.shouldSuffocate(this.world, incorrectSuffocationPos)) {
                        if (suffocationShape == null) {
                            suffocationShape = VoxelShapes.cuboid(new Box(minX, minY, minZ, maxX, maxY, maxZ));
                        }
                        if (VoxelShapes.matchesAnywhere(blockState.getCollisionShape(this.world, incorrectSuffocationPos).offset(vec3d.x, vec3d.y, vec3d.z), suffocationShape, BooleanBiFunction.AND)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
