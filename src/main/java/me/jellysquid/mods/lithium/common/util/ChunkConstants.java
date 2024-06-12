package me.jellysquid.mods.lithium.common.util;

import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.WorldChunk;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class ChunkConstants {
    public static final WorldChunk DUMMY_CHUNK;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            Unsafe unsafe = (Unsafe) f.get(null);
            DUMMY_CHUNK = (WorldChunk) unsafe.allocateInstance(EmptyChunk.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
