package me.jellysquid.mods.lithium.mixin.entity.chunk_cache;

import me.jellysquid.mods.lithium.common.LithiumMod;
import me.jellysquid.mods.lithium.common.cache.EntityChunkCache;
import me.jellysquid.mods.lithium.common.entity.EntityWithChunkCache;
import me.jellysquid.mods.lithium.common.shapes.LithiumVoxelShapes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.tag.Tag;
import net.minecraft.util.ReusableStream;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.ViewableWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity implements EntityWithChunkCache {
    @Shadow
    public abstract Entity getVehicle();

    @Shadow
    public World world;

    @Shadow
    public double y;

    @Shadow
    public double x;

    @Shadow
    public double z;

    @Shadow
    public abstract float getStandingEyeHeight();

    @Shadow
    public abstract Box getBoundingBox();

    private EntityChunkCache chunkCache;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstructed(EntityType<?> type, World world, CallbackInfo ci) {
        this.chunkCache = new EntityChunkCache((Entity) (Object) this);
    }

    private final BlockPos.Mutable scratchPos = new BlockPos.Mutable();

    /**
     * @reason Use the chunk cache.
     * @author JellySquid
     */
    @Overwrite
    public boolean isSubmergedIn(Tag<Fluid> tag, boolean flag) {
        if (this.getVehicle() instanceof BoatEntity) {
            return false;
        }

        double eyeY = this.y + (double) this.getStandingEyeHeight();

        int bX = MathHelper.floor(this.x);
        int bY = MathHelper.floor(eyeY);
        int bZ = MathHelper.floor(this.z);

        FluidState fluid = this.chunkCache.getFluidState(bX, bY, bZ);

        if (fluid.matches(tag)) {
            return eyeY < (bY + (double) fluid.getHeight(this.world, this.scratchPos.set(bX, bY, bZ)) + 0.11111111D);
        }

        return false;
    }

    /**
     * @reason Use the chunk cache.
     * @author JellySquid
     */
    @Overwrite
    private boolean isInsideBubbleColumn() {
        return this.chunkCache.getBlockState(MathHelper.floor(this.x), MathHelper.floor(this.y), MathHelper.floor(this.z)).getBlock() == Blocks.BUBBLE_COLUMN;
    }

    @Inject(method = "baseTick", at = @At("HEAD"))
    private void onBaseTick(CallbackInfo ci) {
        if (LithiumMod.CONFIG.entity.useChunkCacheForEntities) {
            this.chunkCache.updateChunks(this.getBoundingBox());
        }
    }

    @Redirect(method = {"move", "checkBlockCollision", "playStepSound", "isInsideWall"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
    private BlockState redirectGetBlockState(World world, BlockPos pos) {
        return this.chunkCache == null ? world.getBlockState(pos) : this.chunkCache.getBlockState(pos);
    }

    @Redirect(method = {"updateMovementInFluid", "isSubmergedIn"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/FluidState;"))
    private FluidState redirectGetFluidState(World world, BlockPos pos) {
        return this.chunkCache == null ? world.getFluidState(pos) : this.chunkCache.getFluidState(pos);
    }

    @Override
    public EntityChunkCache getEntityChunkCache() {
        return this.chunkCache;
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;doesAreaContainFireSource(Lnet/minecraft/util/math/Box;)Z"))
    private boolean redirectDoesAreaContainFireSource(World world, Box box) {
        int minX = MathHelper.floor(box.minX);
        int maxX = MathHelper.ceil(box.maxX);
        int minY = MathHelper.floor(box.minY);
        int maxY = MathHelper.ceil(box.maxY);
        int minZ = MathHelper.floor(box.minZ);
        int maxZ = MathHelper.ceil(box.maxZ);

        for (int x = minX; x < maxX; ++x) {
            for (int y = minY; y < maxY; ++y) {
                for (int z = minZ; z < maxZ; ++z) {
                    Block block = this.chunkCache.getBlockState(x, y, z).getBlock();

                    if (block == Blocks.FIRE || block == Blocks.LAVA) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Redirect(method = "calculateMotionVector",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;calculateTangentialMotionVector(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Lnet/minecraft/world/ViewableWorld;Lnet/minecraft/entity/EntityContext;Lnet/minecraft/util/ReusableStream;)Lnet/minecraft/util/math/Vec3d;"))
    private static Vec3d redirectCalculateTangentialMotionVector(Vec3d vec, Box box, ViewableWorld world, EntityContext context, ReusableStream<VoxelShape> reusableStream, Entity entity, Vec3d dup0, Box dup1, World dup3, EntityContext dup4, ReusableStream<VoxelShape> dup5) {
        if (entity == null) {
            return Entity.calculateTangentialMotionVector(vec, box, world, context, reusableStream);
        }

        EntityChunkCache chunkCache = ((EntityWithChunkCache) entity).getEntityChunkCache();

        double x = vec.x;
        double y = vec.y;
        double z = vec.z;

        if (y != 0.0D) {
            y = LithiumVoxelShapes.calculateSoftOffset(Direction.Axis.Y, box, chunkCache, y, context, reusableStream.stream());

            if (y != 0.0D) {
                box = box.offset(0.0D, y, 0.0D);
            }
        }

        boolean flag = Math.abs(x) < Math.abs(z);

        if (flag && z != 0.0D) {
            z = LithiumVoxelShapes.calculateSoftOffset(Direction.Axis.Z, box, chunkCache, z, context, reusableStream.stream());

            if (z != 0.0D) {
                box = box.offset(0.0D, 0.0D, z);
            }
        }

        if (x != 0.0D) {
            x = LithiumVoxelShapes.calculateSoftOffset(Direction.Axis.X, box, chunkCache, x, context, reusableStream.stream());

            if (!flag && x != 0.0D) {
                box = box.offset(x, 0.0D, 0.0D);
            }
        }

        if (!flag && z != 0.0D) {
            z = LithiumVoxelShapes.calculateSoftOffset(Direction.Axis.Z, box, chunkCache, z, context, reusableStream.stream());
        }

        return new Vec3d(x, y, z);
    }
}
