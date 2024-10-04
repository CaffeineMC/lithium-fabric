package me.jellysquid.mods.lithium.mixin.math.sine_lut;

import me.jellysquid.mods.lithium.common.util.math.CompactSineLUT;
import net.minecraft.util.math.MathHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MathHelper.class)
public class MixinMathHelper {
    @Shadow
    @Mutable
    private static float[] SINE_TABLE;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onClassInit(CallbackInfo ci) {
        new CompactSineLUT(); // Force class initialisation
        MixinMathHelper.SINE_TABLE = null;
    }

    /**
     * @author jellysquid3
     * @reason use an optimized implementation
     */
    @Overwrite
    public static float sin(float f) {
        return CompactSineLUT.sin(f);
    }

    /**
     * @author jellysquid3
     * @reason use an optimized implementation
     */
    @Overwrite
    public static float cos(float f) {
        return CompactSineLUT.cos(f);
    }
}
