package me.jellysquid.mods.lithium.common.util.unsafe;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeUtil {
    private static final Unsafe INSTANCE;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);

            INSTANCE = (Unsafe) field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not initialize unsafe utilities", e);
        }
    }

    public static Unsafe getInstance() {
        return INSTANCE;
    }
}
