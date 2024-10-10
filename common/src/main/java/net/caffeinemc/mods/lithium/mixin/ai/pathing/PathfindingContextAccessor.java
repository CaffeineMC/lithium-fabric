package net.caffeinemc.mods.lithium.mixin.ai.pathing;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PathfindingContext.class)
public interface PathfindingContextAccessor {

    @Accessor("mutablePos")
    BlockPos.MutableBlockPos getLastNodePos();

}
