package me.jellysquid.mods.lithium.mixin.math.fast_util;

import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Direction.class)
public class MixinDirection {
    @Shadow
    @Final
    private static Direction[] ALL;

    @Shadow
    @Final
    private int idOpposite;

    /**
     * @reason Avoid the modulo/abs operations
     * @author JellySquid
     */
    @Overwrite
    public Direction getOpposite() {
        return ALL[this.idOpposite];
    }
}
