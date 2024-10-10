package net.caffeinemc.mods.lithium.mixin.chunk.no_validation;

import net.minecraft.util.ZeroBitStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ZeroBitStorage.class)
public class ZeroBitStorageMixin {
    @Redirect(
            method = {"getAndSet(II)I", "set(II)V", "get(I)I"},
            at = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/Validate;inclusiveBetween(JJJ)V", remap = false)

    )
    public void skipValidation(long start, long end, long value) {
    }
}
