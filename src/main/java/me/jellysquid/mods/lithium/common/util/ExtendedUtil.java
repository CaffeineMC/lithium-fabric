package me.jellysquid.mods.lithium.common.util;

import net.minecraft.util.Util;

public class ExtendedUtil {
    public static long initTime = 0;

    /**
     * Initializes a new timer source and determines what time the timer starts at. This will overwrite any
     * already installed timer system. In particular, the timer implementation in a Minecraft client is initialized
     * to use GLFW's getTime function, but this causes CPU stalling when accessed from different threads (i.e. the
     * client and server threads) simultaneously. Replacing it can yield a bit of improvement.
     */
    public static void initTimeSource() {
        Util.nanoTimeSupplier = System::nanoTime;
        initTime = Util.nanoTimeSupplier.getAsLong();
    }
}
