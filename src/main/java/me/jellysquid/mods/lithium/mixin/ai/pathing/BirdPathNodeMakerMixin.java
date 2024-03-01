package me.jellysquid.mods.lithium.mixin.ai.pathing;

import me.jellysquid.mods.lithium.common.ai.pathing.PathNodeCache;
import net.minecraft.class_9316;
import net.minecraft.entity.ai.pathing.BirdPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BirdPathNodeMaker.class)
public class BirdPathNodeMakerMixin {

    /**
     * @reason Use optimized implementation which avoids scanning blocks for dangers where possible
     * @author JellySquid, 2No2Name
     */
    @Redirect(method = "getDefaultNodeType", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/pathing/BirdPathNodeMaker;getNodeTypeFromNeighbors(Lnet/minecraft/class_9316;IIILnet/minecraft/entity/ai/pathing/PathNodeType;)Lnet/minecraft/entity/ai/pathing/PathNodeType;"))
    private PathNodeType getNodeTypeFromNeighbors(class_9316 context, int x, int y, int z, PathNodeType type) {
        return PathNodeCache.getNodeTypeFromNeighbors(context, type);
    }
}
