package net.caffeinemc.mods.lithium.common.ai.pathing;

import net.minecraft.world.level.pathfinder.PathType;

public interface BlockStatePathingCache {
    PathType lithium$getPathNodeType();

    PathType lithium$getNeighborPathNodeType();

    void lithium$clearPathTypeCache();

    void lithium$initializePathNodeTypeCache();
}
