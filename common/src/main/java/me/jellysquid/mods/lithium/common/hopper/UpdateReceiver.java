package me.jellysquid.mods.lithium.common.hopper;

import net.minecraft.core.Direction;

public interface UpdateReceiver {
    void lithium$invalidateCacheOnNeighborUpdate(boolean above);

    void lithium$invalidateCacheOnNeighborUpdate(Direction fromDirection);
}
