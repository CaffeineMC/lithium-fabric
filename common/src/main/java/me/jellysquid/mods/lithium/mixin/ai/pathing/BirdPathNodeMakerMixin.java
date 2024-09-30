package me.jellysquid.mods.lithium.mixin.ai.pathing;

import me.jellysquid.mods.lithium.common.ai.pathing.PathNodeCache;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FlyNodeEvaluator.class)
public class BirdPathNodeMakerMixin {

    /**
     * @reason Use optimized implementation which avoids scanning blocks for dangers where possible
     * @author JellySquid, 2No2Name
     */
    @Redirect(method = "getPathType", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/pathfinder/FlyNodeEvaluator;checkNeighbourBlocks(Lnet/minecraft/world/level/pathfinder/PathfindingContext;IIILnet/minecraft/world/level/pathfinder/PathType;)Lnet/minecraft/world/level/pathfinder/PathType;"))
    private PathType getNodeTypeFromNeighbors(PathfindingContext pathContext, int x, int y, int z, PathType pathNodeType) {
        return PathNodeCache.getNodeTypeFromNeighbors(pathContext, x, y, z, pathNodeType);
    }
}
