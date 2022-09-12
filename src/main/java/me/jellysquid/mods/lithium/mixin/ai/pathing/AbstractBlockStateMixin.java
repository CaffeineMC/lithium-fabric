package me.jellysquid.mods.lithium.mixin.ai.pathing;

import me.jellysquid.mods.lithium.common.ai.pathing.BlockStatePathingCache;
import me.jellysquid.mods.lithium.common.ai.pathing.PathNodeDefaults;
import net.minecraft.block.AbstractBlock;
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
        PathNodeType fabricPathNodeType = null;
        PathNodeType fabricNeighborPathNodeType = null;

        // todo after fabric-api is updated, add it "as reference" (not dependency), and use this "if", when loaded.
        // This will load custom node types provided by other mods.
        //if (state instanceof FabricAbstractBlockState fstate) {
        //    fabricPathNodeType = fstate.getPathNodeType();
        //    fabricNeighborPathNodeType = fstate.getNeighborPathNodeType();
        //}

        this.pathNodeType = fabricPathNodeType != null ? fabricPathNodeType : Validate.notNull(PathNodeDefaults.getNodeType(state));
        this.pathNodeTypeNeighbor = fabricNeighborPathNodeType != null ? fabricNeighborPathNodeType : Validate.notNull(PathNodeDefaults.getNeighborNodeType(state));
    }

    @Override
    public PathNodeType getLithiumPathNodeType() {
        return this.pathNodeType;
    }

    @Override
    public PathNodeType getLithiumNeighborPathNodeType() {
        return this.pathNodeTypeNeighbor;
    }

    @Shadow
    protected abstract BlockState asBlockState();
}
