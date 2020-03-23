package me.jellysquid.mods.lithium.mixin.avoid_allocations;

import net.minecraft.block.PistonBlock;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PistonBlock.class)
public class MixinPistonBlock {
    private static final Direction[] DIRECTIONS = Direction.values();

    @Redirect(method = "shouldExtend", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Direction;values()[Lnet/minecraft/util/math/Direction;"))
    private Direction[] redirectShouldExtendDirectionValues() {
        return DIRECTIONS;
    }
}
