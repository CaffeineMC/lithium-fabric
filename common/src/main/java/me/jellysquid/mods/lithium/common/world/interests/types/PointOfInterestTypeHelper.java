package me.jellysquid.mods.lithium.common.world.interests.types;

import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;

public class PointOfInterestTypeHelper {
    private static Predicate<BlockState> POI_BLOCKSTATE_PREDICATE;


    public static void init(Set<BlockState> types) {
        if (POI_BLOCKSTATE_PREDICATE != null) {
            throw new IllegalStateException("Already initialized");
        }

        POI_BLOCKSTATE_PREDICATE = types::contains;
    }

    public static boolean shouldScan(LevelChunkSection section) {
        return section.maybeHas(POI_BLOCKSTATE_PREDICATE);
    }
}
