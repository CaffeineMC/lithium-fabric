package me.jellysquid.mods.lithium.common.world.chunk;

import net.minecraft.world.event.listener.GameEventDispatcher;
import org.jetbrains.annotations.Nullable;

public interface ChunkWithEmptyGameEventDispatcher {

    @Nullable GameEventDispatcher lithium$getExistingGameEventDispatcher(int ySectionCoord);
}
