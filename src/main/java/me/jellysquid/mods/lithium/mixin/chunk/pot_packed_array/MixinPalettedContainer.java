package me.jellysquid.mods.lithium.mixin.chunk.pot_packed_array;

import me.jellysquid.mods.lithium.common.util.LithiumMath;
import me.jellysquid.mods.lithium.common.util.palette.POTPackedIntegerArray;
import net.minecraft.util.PackedIntegerArray;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PalettedContainer.class)
public class MixinPalettedContainer {
    @Redirect(method = "setPaletteSize", at = @At(value = "NEW", target = "net/minecraft/util/PackedIntegerArray"))
    private PackedIntegerArray replaceIntegerArray(int bits, int size) {
        return new POTPackedIntegerArray(bits, size);
    }

    @ModifyVariable(method = "setPaletteSize", at = @At("HEAD"), index = 1, name = "int_1")
    private int afterPaletteSizeChanged(int old) {
        return LithiumMath.nextPowerOfTwo(old);
    }
}
