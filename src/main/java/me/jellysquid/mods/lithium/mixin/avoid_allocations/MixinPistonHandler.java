package me.jellysquid.mods.lithium.mixin.avoid_allocations;

import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PistonHandler.class)
public class MixinPistonHandler {
    private static final Direction[] VALUES = Direction.values();

    @Redirect(method = "canMoveAdjacentBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Direction;values()[Lnet/minecraft/util/math/Direction;"))
    private Direction[] redirectCanMoveAdjacentBlockValues() {
        return VALUES;
    }
}
