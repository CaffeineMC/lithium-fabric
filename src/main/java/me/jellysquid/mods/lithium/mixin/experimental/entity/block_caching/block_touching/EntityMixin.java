package me.jellysquid.mods.lithium.mixin.experimental.entity.block_caching.block_touching;

import me.jellysquid.mods.lithium.common.block.BlockStateFlagHolder;
import me.jellysquid.mods.lithium.common.block.BlockStateFlags;
import me.jellysquid.mods.lithium.common.entity.block_tracking.BlockCache;
import me.jellysquid.mods.lithium.common.entity.block_tracking.BlockCacheProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * This mixin uses the block caching system to be able to skip entity block interactions when the entity is not a player
 * and the nearby blocks cannot be interacted with by touching them.
 */
@Mixin(Entity.class)
public abstract class EntityMixin implements BlockCacheProvider {
    @Inject(
            method = "checkInsideBlocks()V",
            at = @At("HEAD"), cancellable = true
    )
    private void cancelIfSkippable(CallbackInfo ci) {
        //noinspection ConstantConditions
        if (!((Object) this instanceof ServerPlayer)) {
            BlockCache bc = this.getUpdatedBlockCache((Entity)(Object)this);
            if (bc.canSkipBlockTouching()) {
                ci.cancel();
            }
        }
    }

    @Inject(
            method = "checkInsideBlocks()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;getX()I", ordinal = 0)
    )
    private void assumeNoTouchableBlock(CallbackInfo ci) {
        BlockCache bc = this.lithium$getBlockCache();
        if (bc.isTracking()) {
            bc.setCanSkipBlockTouching(true);
        }
    }

    @Inject(
            method = "checkInsideBlocks()V", locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;entityInside(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)V")
    )
    private void checkTouchableBlock(CallbackInfo ci, AABB box, BlockPos blockPos, BlockPos blockPos2, BlockPos.MutableBlockPos mutable, int i, int j, int k, BlockState blockState) {
        BlockCache bc = this.lithium$getBlockCache();
        if (bc.canSkipBlockTouching() &&
                0 != (((BlockStateFlagHolder)blockState).lithium$getAllFlags() & 1 << BlockStateFlags.ENTITY_TOUCHABLE.getIndex())
        ) {
            bc.setCanSkipBlockTouching(false);
        }
    }
}
