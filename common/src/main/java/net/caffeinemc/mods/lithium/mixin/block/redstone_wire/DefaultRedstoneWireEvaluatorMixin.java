package net.caffeinemc.mods.lithium.mixin.block.redstone_wire;

import net.caffeinemc.mods.lithium.common.block.redstone.RedstoneWirePowerCalculations;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedstoneWireBlock;
import net.minecraft.world.level.redstone.DefaultRedstoneWireEvaluator;
import net.minecraft.world.level.redstone.RedstoneWireEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DefaultRedstoneWireEvaluator.class)
public abstract class DefaultRedstoneWireEvaluatorMixin extends RedstoneWireEvaluator {

    private DefaultRedstoneWireEvaluatorMixin(RedstoneWireBlock wireBlock) {
        super(wireBlock);
    }

    @Inject(
            method = "calculateTargetStrength(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)I",
            cancellable = true,
            at = @At(
                    value = "HEAD"
            )
    )
    private void calculateTargetStrengthFaster(Level level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(RedstoneWirePowerCalculations.getNeighborSignal(this.wireBlock, this, level, pos, false, false));
    }
}
