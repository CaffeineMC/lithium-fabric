package me.jellysquid.mods.lithium.mixin.ai.pathing;

import me.jellysquid.mods.lithium.common.ai.pathing.PathNodeCache;
import net.minecraft.entity.ai.pathing.BirdPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathContext;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import me.jellysquid.mods.lithium.common.ai.pathing.NodePosition;

@Mixin(BirdPathNodeMaker.class)
public class BirdPathNodeMakerMixin {

    @Redirect(method = "getDefaultNodeType", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/pathing/PathNodeCache;getNodeTypeFromNeighbors(Lnet/minecraft/entity/ai/pathing/PathContext;Lme/jellysquid/mods/lithium/common/ai/pathing/NodePosition;Lnet/minecraft/entity/ai/pathing/PathNodeType;)Lnet/minecraft/entity/ai/pathing/PathNodeType;"))
    private PathNodeType getNodeTypeFromNeighbors(PathContext pathContext, BlockPos pos, PathNodeType fallback) {
        NodePosition nodePos = new NodePosition(pos.getX(), pos.getY(), pos.getZ());
        return PathNodeCache.getNodeTypeFromNeighbors(pathContext, nodePos, fallback);
    }
}