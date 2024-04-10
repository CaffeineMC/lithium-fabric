package me.jellysquid.mods.lithium.common.ai.pathing;

import net.minecraft.entity.ai.pathing.PathNodeType;

public interface BlockStatePathingCache {
    PathNodeType lithium$getPathNodeType();

    PathNodeType lithium$getNeighborPathNodeType();
}
