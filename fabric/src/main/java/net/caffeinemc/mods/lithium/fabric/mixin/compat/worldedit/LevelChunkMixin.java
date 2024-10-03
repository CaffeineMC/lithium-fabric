package net.caffeinemc.mods.lithium.fabric.mixin.compat.worldedit;

import net.caffeinemc.mods.lithium.common.hopper.UpdateReceiver;
import net.caffeinemc.mods.lithium.common.util.DirectionConstants;
import net.caffeinemc.mods.lithium.common.world.WorldHelper;
import net.caffeinemc.mods.lithium.common.world.blockentity.BlockEntityGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {

    @Shadow
    public abstract Level getLevel();

    @Inject(
            method = "setBlockState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;onPlace(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V", shift = At.Shift.BEFORE)
    )
    private void updateHoppersIfWorldEditPresent(BlockPos pos, BlockState state, boolean moved, CallbackInfoReturnable<BlockState> cir) {
        if ((state.getBlock() instanceof WorldlyContainerHolder || state.hasBlockEntity())) {
            lithium$updateHopperCachesOnNewInventoryAdded((LevelChunk) (Object) this, pos, this.getLevel());
        }
    }

    @Unique
    private static void lithium$updateHopperCachesOnNewInventoryAdded(LevelChunk worldChunk, BlockPos pos, Level world) {
        BlockPos.MutableBlockPos neighborPos = new BlockPos.MutableBlockPos();
        for (Direction offsetDirection : DirectionConstants.ALL) {
            neighborPos.setWithOffset(pos, offsetDirection);
            BlockEntity neighborBlockEntity =
                    WorldHelper.arePosWithinSameChunk(pos, neighborPos) ?
                            worldChunk.getBlockEntity(neighborPos, LevelChunk.EntityCreationType.CHECK) :
                            ((BlockEntityGetter) world).lithium$getLoadedExistingBlockEntity(neighborPos);
            if (neighborBlockEntity instanceof UpdateReceiver updateReceiver) {
                updateReceiver.lithium$invalidateCacheOnNeighborUpdate(offsetDirection.getOpposite());
            }
        }
    }
}
