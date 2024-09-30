package net.caffeinemc.mods.lithium.mixin.block.moving_block_shapes;

import net.caffeinemc.mods.lithium.common.shapes.OffsetVoxelShapeCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;


/**
 * @author 2No2Name
 */
@Mixin(PistonMovingBlockEntity.class)
public abstract class PistonBlockEntityMixin {
    private static final VoxelShape[] PISTON_BASE_WITH_MOVING_HEAD_SHAPES = precomputePistonBaseWithMovingHeadShapes();

    @Shadow
    private Direction direction;
    @Shadow
    private boolean extending;
    @Shadow
    private boolean isSourcePiston;


    @Shadow
    private BlockState movedState;

    /**
     * Avoid calling {@link Shapes#or(VoxelShape, VoxelShape)} whenever possible - use precomputed merged piston head + base shapes and
     * cache the results for all union calls with an empty shape as first argument. (these are all other cases)
     */
    @Inject(
            method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/Direction;getStepX()I",
                    shift = At.Shift.BEFORE
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    private void skipVoxelShapeUnion(BlockGetter world, BlockPos pos, CallbackInfoReturnable<VoxelShape> cir, VoxelShape voxelShape, Direction direction, BlockState blockState, float f) {
        if (this.extending || !this.isSourcePiston || !(this.movedState.getBlock() instanceof PistonBaseBlock)) {
            //here voxelShape2.isEmpty() is guaranteed, vanilla code would call union() which calls simplify()
            VoxelShape blockShape = blockState.getCollisionShape(world, pos);

            //we cache the simplified shapes, as the simplify() method costs a lot of CPU time and allocates several objects
            VoxelShape offsetAndSimplified = getOffsetAndSimplified(blockShape, Math.abs(f), f < 0f ? this.direction.getOpposite() : this.direction);
            cir.setReturnValue(offsetAndSimplified);
        } else {
            //retracting piston heads have to act like their base as well, as the base block is replaced with the moving block
            //f >= 0f is guaranteed (assuming no other mod interferes)
            int index = getIndexForMergedShape(f, this.direction);
            cir.setReturnValue(PISTON_BASE_WITH_MOVING_HEAD_SHAPES[index]);
        }
    }

    /**
     * We cache the offset and simplified VoxelShapes that are otherwise constructed on every call of getCollisionShape.
     * For each offset direction and distance (6 directions, 2 distances each, and no direction with 0 distance) we
     * store the offset and simplified VoxelShapes in the original VoxelShape when they are accessed the first time.
     * We use safe publication, because both the Render and Server thread are using the cache.
     *
     * @param blockShape the original shape, must not be modified after passing it as an argument to this method
     * @param offset     the offset distance
     * @param direction  the offset direction
     * @return blockShape offset and simplified
     */
    private static VoxelShape getOffsetAndSimplified(VoxelShape blockShape, float offset, Direction direction) {
        VoxelShape offsetSimplifiedShape = ((OffsetVoxelShapeCache) blockShape).lithium$getOffsetSimplifiedShape(offset, direction);
        if (offsetSimplifiedShape == null) {
            //create the offset shape and store it for later use
            offsetSimplifiedShape = blockShape.move(direction.getStepX() * offset, direction.getStepY() * offset, direction.getStepZ() * offset).optimize();
            ((OffsetVoxelShapeCache) blockShape).lithium$setShape(offset, direction, offsetSimplifiedShape);
        }
        return offsetSimplifiedShape;
    }

    /**
     * Precompute all 18 possible configurations for the merged piston base and head shape.
     *
     * @return The array of the merged VoxelShapes, indexed by {@link PistonBlockEntityMixin#getIndexForMergedShape(float, Direction)}
     */
    private static VoxelShape[] precomputePistonBaseWithMovingHeadShapes() {
        float[] offsets = {0f, 0.5f, 1f};
        Direction[] directions = Direction.values();

        VoxelShape[] mergedShapes = new VoxelShape[offsets.length * directions.length];

        for (Direction facing : directions) {
            VoxelShape baseShape = Blocks.PISTON.defaultBlockState().setValue(PistonBaseBlock.EXTENDED, true)
                    .setValue(PistonBaseBlock.FACING, facing).getCollisionShape(null, null);
            for (float offset : offsets) {
                //this cache is only required for the merged piston head + base shape.
                //this shape is only used when !this.extending
                //here: isShort = this.extending != 1.0F - this.progress < 0.25F can be simplified to:
                //isShort = f < 0.25F , because f = getAmountExtended(this.progress) can be simplified to f == 1.0F - this.progress
                //therefore isShort is dependent on the offset:
                boolean isShort = offset < 0.25f;

                VoxelShape headShape = (Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.FACING, facing))
                        .setValue(PistonHeadBlock.SHORT, isShort).getCollisionShape(null, null);

                VoxelShape offsetHead = headShape.move(facing.getStepX() * offset,
                        facing.getStepY() * offset,
                        facing.getStepZ() * offset);
                mergedShapes[getIndexForMergedShape(offset, facing)] = Shapes.or(baseShape, offsetHead);
            }

        }

        return mergedShapes;
    }

    private static int getIndexForMergedShape(float offset, Direction direction) {
        if (offset != 0f && offset != 0.5f && offset != 1f) {
            return -1;
        }
        //shape of offset 0 is still dependent on the direction, due to piston head and base being directional blocks
        return (int) (2 * offset) + (3 * direction.get3DDataValue());
    }
}
