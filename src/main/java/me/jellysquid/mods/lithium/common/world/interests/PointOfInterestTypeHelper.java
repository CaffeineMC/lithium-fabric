package me.jellysquid.mods.lithium.common.world.interests;

import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.ChunkSection;

import java.util.Set;

public class PointOfInterestTypeHelper {
    private static Set<BlockState> TYPES;

    public static void init(Set<BlockState> types) {
        if (TYPES != null) {
            throw new IllegalStateException("Already initialized");
        }

        TYPES = types;
    }

    public static boolean shouldScan(ChunkSection section) {
        for (BlockState state : TYPES) {
            if (section.method_19523(state)) {
                return true;
            }
        }

        return false;
    }

}
