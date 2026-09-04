package net.caffeinemc.mods.lithium.mixin.block.redstone_wire;

import net.caffeinemc.mods.lithium.common.block.redstone.RedstoneWirePowerCalculations;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedstoneWireBlock;
import net.minecraft.world.level.redstone.RedstoneWireEvaluator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedstoneWireEvaluator.class)
public class RedstoneWireEvaluatorMixin {

    @Shadow
    @Final
    private RedstoneWireBlock wireBlock;

    @Inject(
            method = "getIncomingWireSignal(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)I",
            cancellable = true,
            at = @At(
                    value = "HEAD"
            )
    )
    private void getIncomingWireSignalFaster(Level level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(RedstoneWirePowerCalculations.getNeighborWireSignal(this.wireBlock, (RedstoneWireEvaluator) (Object) this, level, pos));
    }
}
