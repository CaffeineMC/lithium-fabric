package me.jellysquid.mods.lithium.common.ai.pathing;

import net.minecraft.entity.ai.pathing.PathNodeType;

public interface BlockStatePathingCache {
    PathNodeType getLithiumPathNodeType();

    PathNodeType getLithiumNeighborPathNodeType();
}
