package me.jellysquid.mods.lithium.mixin.shapes.blockstate_cache;

import me.jellysquid.mods.lithium.common.block.BlockShapeCacheExtended;
import me.jellysquid.mods.lithium.common.block.BlockShapeCacheExtendedProvider;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class AbstractBlockStateMixin implements BlockShapeCacheExtendedProvider {
    @Shadow
    protected BlockState.ShapeCache shapeCache;

    @SuppressWarnings("ConstantConditions")
    @Override
    public BlockShapeCacheExtended getExtendedShapeCache() {
        return (BlockShapeCacheExtended) (Object) this.shapeCache;
    }
}
