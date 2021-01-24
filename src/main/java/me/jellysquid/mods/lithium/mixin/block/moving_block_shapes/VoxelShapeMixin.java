package me.jellysquid.mods.lithium.mixin.block.moving_block_shapes;

import me.jellysquid.mods.lithium.common.shapes.OffsetVoxelShapeCache;
import me.jellysquid.mods.lithium.common.util.tuples.FinalObject;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(VoxelShape.class)
public class VoxelShapeMixin implements OffsetVoxelShapeCache {
    private FinalObject<VoxelShape>[] offsetAndSimplified;

    public void setShape(float offset, Direction direction, VoxelShape offsetShape) {
        if (offsetShape == null) {
            throw new IllegalArgumentException("offsetShape must not be null!");
        }
        int index = getIndexForOffsetSimplifiedShapes(offset, direction);
        FinalObject<VoxelShape>[] offsetAndSimplified = this.offsetAndSimplified;
        if (offsetAndSimplified == null) {
            //noinspection unchecked
            this.offsetAndSimplified = (offsetAndSimplified = new FinalObject[1 + (2 * 6)]);
        }
        //using FinalObject as it stores the value in a final field, which guarantees safe publication
        offsetAndSimplified[index] = new FinalObject<>(offsetShape);
    }

    public VoxelShape getOffsetSimplifiedShape(float offset, Direction direction) {
        FinalObject<VoxelShape>[] offsetAndSimplified = this.offsetAndSimplified;
        if (offsetAndSimplified == null) {
            return null;
        }
        int index = getIndexForOffsetSimplifiedShapes(offset, direction);
        //usage of FinalObject guarantees that we are seeing a fully initialized VoxelShape here, even when it was created on a different thread
        FinalObject<VoxelShape> wrappedShape = offsetAndSimplified[index];
        //noinspection FinalObjectAssignedToNull,FinalObjectGetWithoutIsPresent
        return wrappedShape == null ? null : wrappedShape.getValue();
    }

    private static int getIndexForOffsetSimplifiedShapes(float offset, Direction direction) {
        if (offset != 0f && offset != 0.5f && offset != 1f) {
            throw new IllegalArgumentException("offset must be one of {0f, 0.5f, 1f}");
        }
        if (offset == 0f) {
            return 0; //can treat offsetting by 0 in all directions the same
        }
        return (int) (2 * offset) + 2 * direction.getId();
    }
}
