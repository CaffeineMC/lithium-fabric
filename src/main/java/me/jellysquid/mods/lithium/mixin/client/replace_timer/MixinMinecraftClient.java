package me.jellysquid.mods.lithium.mixin.client.replace_timer;

import com.mojang.blaze3d.systems.RenderSystem;
import me.jellysquid.mods.lithium.common.util.ExtendedUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.LongSupplier;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    /**
     * Replace the timer function.
     */
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;initBackendSystem()Ljava/util/function/LongSupplier;"))
    private LongSupplier redirectBackendInit() {
        RenderSystem.initBackendSystem();
        ExtendedUtil.initTimeSource();

        return Util.nanoTimeSupplier;
    }
}
