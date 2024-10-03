package net.caffeinemc.mods.lithium.mixin.alloc.enum_values.redstone_wire;

import net.caffeinemc.mods.lithium.common.util.DirectionConstants;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RedStoneWireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RedStoneWireBlock.class)
public class RedStoneWireBlockMixin {

    @Redirect(
            method = "updatePowerStrength(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Direction;values()[Lnet/minecraft/core/Direction;")
    )
    private Direction[] removeAllocation1() {
        return DirectionConstants.ALL;
    }

    @Redirect(
            method = "checkCornerChangeAt(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Direction;values()[Lnet/minecraft/core/Direction;")
    )
    private Direction[] removeAllocation2() {
        return DirectionConstants.ALL;
    }
}
