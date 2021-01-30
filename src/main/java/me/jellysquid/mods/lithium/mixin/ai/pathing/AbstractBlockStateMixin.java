package me.jellysquid.mods.lithium.mixin.ai.pathing;

import me.jellysquid.mods.lithium.api.pathing.BlockPathingBehavior;
import me.jellysquid.mods.lithium.common.ai.pathing.BlockStatePathingCache;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.PathNodeType;
import org.apache.commons.lang3.Validate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin implements BlockStatePathingCache {
    private PathNodeType pathNodeType = PathNodeType.OPEN;
    private PathNodeType pathNodeTypeNeighbor = PathNodeType.OPEN;

    @Inject(method = "initShapeCache()V", at = @At("RETURN"))
    private void init(CallbackInfo ci) {
        BlockState state = this.asBlockState();
        BlockPathingBehavior behavior = (BlockPathingBehavior) this.getBlock();

        this.pathNodeType = Validate.notNull(behavior.getPathNodeType(state));
        this.pathNodeTypeNeighbor = Validate.notNull(behavior.getPathNodeTypeAsNeighbor(state));
    }

    @Override
    public PathNodeType getPathNodeType() {
        return this.pathNodeType;
    }

    @Override
    public PathNodeType getNeighborPathNodeType() {
        return this.pathNodeTypeNeighbor;
    }

    @Shadow
    protected abstract BlockState asBlockState();

    @Shadow
    public abstract Block getBlock();
}
