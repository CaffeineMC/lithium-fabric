package me.jellysquid.mods.lithium.mixin.ai.pathing;

import me.jellysquid.mods.lithium.common.ai.pathing.BlockStatePathingCache;
import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.ai.pathing.PathNodeType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin implements BlockStatePathingCache {
    private PathNodeType pathNodeType = null;
    private PathNodeType pathNodeTypeNeighbor = null;

    @Override
    public PathNodeType getCachedPathNodeType() {
        return this.pathNodeType;
    }

    @Override
    public PathNodeType getCachedNeighborPathNodeType() {
        return this.pathNodeTypeNeighbor;
    }

    @Override
    public void setCachedPathNodeType(PathNodeType type) {
        this.pathNodeType = type;
    }

    @Override
    public void setCachedNeighborPathNodeType(PathNodeType type) {
        this.pathNodeTypeNeighbor = type;
    }
}
