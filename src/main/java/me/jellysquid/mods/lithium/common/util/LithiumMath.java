package me.jellysquid.mods.lithium.common.util;

public class LithiumMath {
    public static int roundUp(int num, int interval) {
        return ((num + (interval - 1)) / interval) * interval;
    }
}
