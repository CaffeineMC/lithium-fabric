package me.jellysquid.mods.lithium.mixin.ai.pathing;

import me.jellysquid.mods.lithium.api.pathing.BlockPathingBehavior;
import me.jellysquid.mods.lithium.common.ai.pathing.PathNodeDefaults;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.PathNodeType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Block.class)
public class MixinBlock implements BlockPathingBehavior {
    @Override
    public PathNodeType getPathNodeType(BlockState state) {
        return PathNodeDefaults.getNodeType(state);
    }

    @Override
    public PathNodeType getPathNodeTypeAsNeighbor(BlockState state) {
        return PathNodeDefaults.getNeighborNodeType(state);
    }
}
