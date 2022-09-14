package me.jellysquid.mods.lithium.common.ai.pathing;

import net.minecraft.entity.ai.pathing.PathNodeType;

public interface BlockStatePathingCache {
    PathNodeType getCachedPathNodeType();

    PathNodeType getCachedNeighborPathNodeType();

    void setCachedPathNodeType(PathNodeType type);

    void setCachedNeighborPathNodeType(PathNodeType type);
}
