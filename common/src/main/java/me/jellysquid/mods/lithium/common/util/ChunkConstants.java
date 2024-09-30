package me.jellysquid.mods.lithium.common.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;

public class ChunkConstants {
    public static final LevelChunk DUMMY_CHUNK;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            Unsafe unsafe = (Unsafe) f.get(null);
            DUMMY_CHUNK = (LevelChunk) unsafe.allocateInstance(EmptyLevelChunk.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
