package me.jellysquid.mods.lithium.common;

import me.jellysquid.mods.lithium.asm.patches.DevirtualizeBlockPosTransformer;

public class LithiumPatcher implements Runnable {
    @Override
    public void run() {
        if (LithiumMod.CONFIG.general.useBlockPosOptimizations) {
            DevirtualizeBlockPosTransformer.install();
        }
    }
}
