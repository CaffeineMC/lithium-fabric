package me.jellysquid.mods.lithium.mixin.voxelshape.block_shape_cache;

import me.jellysquid.mods.lithium.common.block.BlockStateWithShapeCache;
import me.jellysquid.mods.lithium.common.block.ExtendedBlockShapeCache;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class MixinAbstractBlockState implements BlockStateWithShapeCache {
    @Shadow
    protected BlockState.ShapeCache shapeCache;

    @Override
    public ExtendedBlockShapeCache getExtendedShapeCache() {
        return (ExtendedBlockShapeCache) this.shapeCache;
    }
}
