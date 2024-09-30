package net.caffeinemc.mods.lithium.mixin.neoforge.block.hopper;

import net.caffeinemc.mods.lithium.common.hopper.HopperHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public class LevelMixin {

    @Inject(
            method = "markAndNotifyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;II)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;onBlockStateChange(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V")

    )
    private void updateHopperOnUpdateSuppression(BlockPos pos, LevelChunk chunk, BlockState blockState, BlockState blockState2, int flags, int maxUpdateDepth, CallbackInfo ci) {
        HopperHelper.updateHopperOnUpdateSuppression((Level) (Object) this, pos, flags, chunk, blockState != blockState2);
    }
}

