package me.jellysquid.mods.lithium.mixin.client.replace_timer;

import me.jellysquid.mods.lithium.common.util.ExtendedSystemUtil;
import net.minecraft.util.SystemUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.LongSupplier;

@Mixin(SystemUtil.class)
public class MixinSystemUtil {
    @Shadow
    public static LongSupplier nanoTimeSupplier;

    /**
     * Patches the method to return the time passed since timer initialization. We return a relative time
     * instead of an absolute time to mimic the behavior of GLFW's timer.
     *
     * @author JellySquid
     */
    @Overwrite
    public static long getMeasuringTimeNano() {
        return nanoTimeSupplier.getAsLong() - ExtendedSystemUtil.initTime;
    }
}
