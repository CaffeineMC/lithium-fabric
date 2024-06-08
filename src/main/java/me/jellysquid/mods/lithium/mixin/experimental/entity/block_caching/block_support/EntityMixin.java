package me.jellysquid.mods.lithium.mixin.experimental.entity.block_caching.block_support;

import com.llamalad7.mixinextras.sugar.Local;
import me.jellysquid.mods.lithium.common.entity.block_tracking.BlockCache;
import me.jellysquid.mods.lithium.common.entity.block_tracking.BlockCacheProvider;
import me.jellysquid.mods.lithium.common.entity.block_tracking.block_support.SupportingBlockCollisionShapeProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Entity.class)
public abstract class EntityMixin implements BlockCacheProvider, SupportingBlockCollisionShapeProvider {
    @Shadow
    public abstract World getWorld();

    @Shadow
    protected abstract double getGravity();

    @Shadow
    public Optional<BlockPos> supportingBlockPos;

    @Inject(
            method = "updateSupportingBlockPos", cancellable = true,
            at = @At(
                    value = "INVOKE", shift = At.Shift.BEFORE,
                    target = "Lnet/minecraft/entity/Entity;getBoundingBox()Lnet/minecraft/util/math/Box;"
            )
    )
    private void cancelIfSkippable(boolean onGround, Vec3d movement, CallbackInfo ci) {
        if (movement == null || (movement.x == 0 && movement.z == 0)) {
            //noinspection ConstantConditions
            BlockCache bc = this.getUpdatedBlockCache((Entity) (Object) this);
            if (bc.canSkipSupportingBlockSearch()) {
                ci.cancel();
            }
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Inject(
            method = "updateSupportingBlockPos",
            at = @At(
                    value = "INVOKE_ASSIGN", ordinal = 0, shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/world/World;findSupportingBlockPos(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Ljava/util/Optional;"
            )
    )
    private void cacheSupportingBlockSearch(CallbackInfo ci, @Local Optional<BlockPos> pos) {
        BlockCache bc = this.lithium$getBlockCache();
        if (bc.isTracking()) {
            bc.setCanSkipSupportingBlockSearch(true);
            if (pos.isPresent() && this.getGravity() > 0D) {
                bc.cacheSupportingBlock(this.getWorld().getBlockState(pos.get()));
            }
        }
    }

    @Inject(
            method = "updateSupportingBlockPos",
            at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/World;findSupportingBlockPos(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Ljava/util/Optional;")
    )
    private void uncacheSupportingBlockSearch(CallbackInfo ci) {
        BlockCache bc = this.lithium$getBlockCache();
        if (bc.isTracking()) {
            bc.setCanSkipSupportingBlockSearch(false);
        }
    }

    @Inject(
            method = "updateSupportingBlockPos",
            at = @At(value = "INVOKE", target = "Ljava/util/Optional;empty()Ljava/util/Optional;", remap = false)
    )
    private void uncacheSupportingBlockSearch1(boolean onGround, Vec3d movement, CallbackInfo ci) {
        BlockCache bc = this.lithium$getBlockCache();
        if (bc.isTracking()) {
            bc.setCanSkipSupportingBlockSearch(false);
        }
    }

    @Override
    public @Nullable VoxelShape lithium$getCollisionShapeBelow() {
        BlockCache bc = this.getUpdatedBlockCache((Entity) (Object) this);
        if (bc.isTracking()) {
            BlockState cachedSupportingBlock = bc.getCachedSupportingBlock();
            if (cachedSupportingBlock != null && this.supportingBlockPos.isPresent()) {
                BlockPos blockPos = this.supportingBlockPos.get();
                return cachedSupportingBlock.getCollisionShape(this.getWorld(), blockPos, ShapeContext.of((Entity) (Object) this)).offset(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            }
        }
        return null;
    }
}
