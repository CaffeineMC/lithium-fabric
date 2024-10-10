package net.caffeinemc.mods.lithium.mixin.block.moving_block_shapes;

import net.caffeinemc.mods.lithium.common.shapes.OffsetVoxelShapeCache;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(VoxelShape.class)
public class VoxelShapeMixin implements OffsetVoxelShapeCache {
    private volatile VoxelShape[] offsetAndSimplified;

    public void lithium$setShape(float offset, Direction direction, VoxelShape offsetShape) {
        if (offsetShape == null) {
            throw new IllegalArgumentException("offsetShape must not be null!");
        }
        int index = getIndexForOffsetSimplifiedShapes(offset, direction);
        VoxelShape[] offsetAndSimplifiedShapes = this.offsetAndSimplified;
        if (offsetAndSimplifiedShapes == null) {
            offsetAndSimplifiedShapes = new VoxelShape[1 + 2 * 6];
        } else {
            offsetAndSimplifiedShapes = offsetAndSimplifiedShapes.clone();
        }
        offsetAndSimplifiedShapes[index] = offsetShape;
        this.offsetAndSimplified = offsetAndSimplifiedShapes;
    }

    public VoxelShape lithium$getOffsetSimplifiedShape(float offset, Direction direction) {
        VoxelShape[] offsetAndSimplified = this.offsetAndSimplified;
        if (offsetAndSimplified == null) {
            return null;
        }
        int index = getIndexForOffsetSimplifiedShapes(offset, direction);
        return offsetAndSimplified[index];
    }

    private static int getIndexForOffsetSimplifiedShapes(float offset, Direction direction) {
        if (offset != 0f && offset != 0.5f && offset != 1f) {
            throw new IllegalArgumentException("offset must be one of {0f, 0.5f, 1f}");
        }
        if (offset == 0f) {
            return 0; //can treat offsetting by 0 in all directions the same
        }
        return (int) (2 * offset) + 2 * direction.get3DDataValue();
    }
}
