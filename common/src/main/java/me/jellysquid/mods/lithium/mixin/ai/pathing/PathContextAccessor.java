package me.jellysquid.mods.lithium.mixin.ai.pathing;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PathfindingContext.class)
public interface PathContextAccessor {

    @Accessor("mutablePos")
    BlockPos.MutableBlockPos getLastNodePos();

}
