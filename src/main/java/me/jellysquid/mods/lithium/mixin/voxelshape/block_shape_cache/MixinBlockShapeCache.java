package me.jellysquid.mods.lithium.mixin.voxelshape.block_shape_cache;

import me.jellysquid.mods.lithium.common.util.BitUtil;
import me.jellysquid.mods.lithium.common.block.ExtendedBlockShapeCache;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.EmptyBlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockState.ShapeCache.class)
public class MixinBlockShapeCache implements ExtendedBlockShapeCache {
    private byte sideCoversSmallSquare;
    private byte sideCoversMediumSquare;

    private byte sideSolidFullSquare;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(BlockState state, CallbackInfo ci) {
        VoxelShape shape = state.getCollisionShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);

        for (Direction dir : DIRECTIONS) {
            if (!state.matches(BlockTags.LEAVES)) {
                VoxelShape face = shape.getFace(dir);

                this.sideCoversSmallSquare |= BitUtil.bit(dir.ordinal(), calculateSideCoversSquare(face, SOLID_SMALL_SQUARE_SHAPE));
                this.sideCoversMediumSquare |= BitUtil.bit(dir.ordinal(), calculateSideCoversSquare(face, SOLID_MEDIUM_SQUARE_SHAPE));
                this.sideSolidFullSquare |= BitUtil.bit(dir.ordinal(), calculateIsFaceFullSquare(face));
            }
        }
    }

    @Override
    public boolean sideCoversSmallSquare(Direction facing) {
        return BitUtil.contains(this.sideCoversSmallSquare, facing.ordinal());
    }

    @Override
    public boolean sideCoversMediumSquare(Direction facing) {
        return BitUtil.contains(this.sideCoversMediumSquare, facing.ordinal());
    }

    @Override
    public boolean isFaceFullSquare(Direction facing) {
        return BitUtil.contains(this.sideSolidFullSquare, facing.ordinal());
    }

    private static boolean calculateSideCoversSquare(VoxelShape shape, VoxelShape square) {
        return shape == VoxelShapes.fullCube() || !VoxelShapes.matchesAnywhere(shape, square, BooleanBiFunction.ONLY_SECOND);
    }

    private static boolean calculateIsFaceFullSquare(VoxelShape shape) {
        return shape == VoxelShapes.fullCube() || !VoxelShapes.matchesAnywhere(VoxelShapes.fullCube(), shape, BooleanBiFunction.NOT_SAME);
    }

    private static final Direction[] DIRECTIONS = Direction.values();

    private static final VoxelShape SOLID_MEDIUM_SQUARE_SHAPE = VoxelShapes.combineAndSimplify(VoxelShapes.fullCube(), Block.createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D), BooleanBiFunction.ONLY_FIRST);
    private static final VoxelShape SOLID_SMALL_SQUARE_SHAPE = Block.createCuboidShape(7.0D, 0.0D, 7.0D, 9.0D, 10.0D, 9.0D);
}
