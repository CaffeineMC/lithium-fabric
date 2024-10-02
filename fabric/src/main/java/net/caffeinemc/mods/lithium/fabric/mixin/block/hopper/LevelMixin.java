package net.caffeinemc.mods.lithium.fabric.mixin.block.hopper;

import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.lithium.common.hopper.HopperHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public class LevelMixin {

    @Inject(
            method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;onBlockStateChange(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V")

    )
    private void updateHopperOnUpdateSuppression(BlockPos pos, BlockState state, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir, @Local LevelChunk worldChunk, @Local Block block, @Local(ordinal = 1) BlockState blockState, @Local(ordinal = 2) BlockState blockState2) {
        HopperHelper.updateHopperOnUpdateSuppression((Level) (Object) this, pos, flags, worldChunk, blockState != blockState2);
    }
}

