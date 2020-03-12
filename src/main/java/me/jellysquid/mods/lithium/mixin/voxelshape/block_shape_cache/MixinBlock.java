package me.jellysquid.mods.lithium.mixin.voxelshape.block_shape_cache;

import me.jellysquid.mods.lithium.common.block.BlockStateWithShapeCache;
import me.jellysquid.mods.lithium.common.block.ExtendedBlockShapeCache;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Replaces a number of functions in the Block class which are used to determine if some redstone components and other
 * blocks can stand on top of another block. These functions make use of additional cached data in BlockState$ShapeCache.
 */
@Mixin(Block.class)
public class MixinBlock {
    @Shadow
    @Final
    private static VoxelShape SOLID_MEDIUM_SQUARE_SHAPE;

    @Shadow
    @Final
    private static VoxelShape SOLID_SMALL_SQUARE_SHAPE;

    /**
     * @reason Use the shape cache
     * @author JellySquid
     */
    @Overwrite
    public static boolean topCoversMediumSquare(BlockView world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        BlockState.ShapeCache shapeCache = ((BlockStateWithShapeCache) state).bridge$getShapeCache();

        if (shapeCache != null) {
            return ((ExtendedBlockShapeCache) shapeCache).sideCoversMediumSquare(Direction.UP);
        }

        return sideCoversSquare(state, state.getCollisionShape(world, pos).getFace(Direction.UP), SOLID_MEDIUM_SQUARE_SHAPE);
    }

    /**
     * @reason Use the shape cache
     * @author JellySquid
     */
    @Overwrite
    public static boolean sideCoversSmallSquare(WorldView world, BlockPos pos, Direction side) {
        BlockState state = world.getBlockState(pos);
        BlockState.ShapeCache shapeCache = ((BlockStateWithShapeCache) state).bridge$getShapeCache();

        if (shapeCache != null) {
            return ((ExtendedBlockShapeCache) shapeCache).sideCoversSmallSquare(side);
        }

        return sideCoversSquare(state, state.getCollisionShape(world, pos).getFace(side), SOLID_SMALL_SQUARE_SHAPE);
    }

    private static boolean sideCoversSquare(BlockState state, VoxelShape shape, VoxelShape square) {
        if (!state.matches(BlockTags.LEAVES)) {
            // Avoid the expensive call to VoxelShapes#matchesAnywhere if the block in question is a full cube
            if (shape == VoxelShapes.fullCube()) {
                return true;
            }

            return !VoxelShapes.matchesAnywhere(shape, square, BooleanBiFunction.ONLY_SECOND);
        }

        return false;
    }
}
