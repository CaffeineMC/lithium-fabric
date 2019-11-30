package me.jellysquid.mods.lithium.mixin.client.replace_timer;

import me.jellysquid.mods.lithium.common.util.ExtendedSystemUtil;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    /**
     * Hook which takes place right after Minecraft switches timer implementations.
     */
    @Inject(method = "init", at = @At(value = "FIELD", target = "Lnet/minecraft/util/SystemUtil;nanoTimeSupplier:Ljava/util/function/LongSupplier;", shift = At.Shift.AFTER))
    private void afterTimeSet(CallbackInfo ci) {
        ExtendedSystemUtil.initTimeSource();
    }
}
