package me.jellysquid.mods.lithium.common.util.math;

public class Color4 {
    public final int r, g, b, a;

    public Color4(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public static Color4 fromRGBA(int color) {
        return new Color4((color >> 16 & 255), (color >> 8 & 255), (color & 255), (color >> 24 & 255));
    }
}
