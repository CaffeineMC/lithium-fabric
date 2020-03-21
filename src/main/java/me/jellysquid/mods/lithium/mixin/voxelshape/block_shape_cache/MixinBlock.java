package me.jellysquid.mods.lithium.mixin.voxelshape.block_shape_cache;

import me.jellysquid.mods.lithium.common.block.BlockShapeHelper;
import me.jellysquid.mods.lithium.common.block.BlockStateWithShapeCache;
import me.jellysquid.mods.lithium.common.block.ExtendedBlockShapeCache;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Replaces a number of functions in the Block class which are used to determine if some redstone components and other
 * blocks can stand on top of another block. These functions make use of additional cached data in BlockState$ShapeCache.
 */
@Mixin(Block.class)
public class MixinBlock {
    /**
     * @reason Use the shape cache
     * @author JellySquid
     */
    @Overwrite
    public static boolean topCoversMediumSquare(BlockView world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        ExtendedBlockShapeCache shapeCache = ((BlockStateWithShapeCache) state).getExtendedShapeCache();

        if (shapeCache != null) {
            return shapeCache.sideCoversMediumSquare(Direction.UP);
        }

        return BlockShapeHelper.sideCoversSquare(state.getCollisionShape(world, pos).getFace(Direction.UP), BlockShapeHelper.SOLID_MEDIUM_SQUARE_SHAPE);
    }

    /**
     * @reason Use the shape cache
     * @author JellySquid
     */
    @Overwrite
    public static boolean sideCoversSmallSquare(WorldView world, BlockPos pos, Direction side) {
        BlockState state = world.getBlockState(pos);
        ExtendedBlockShapeCache shapeCache = ((BlockStateWithShapeCache) state).getExtendedShapeCache();

        if (shapeCache != null) {
            return shapeCache.sideCoversSmallSquare(side);
        }

        return BlockShapeHelper.sideCoversSquare(state.getCollisionShape(world, pos).getFace(side), BlockShapeHelper.SOLID_SMALL_SQUARE_SHAPE);
    }
}
