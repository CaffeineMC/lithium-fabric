package me.jellysquid.mods.lithium.mixin.shapes.blockstate_cache;

import me.jellysquid.mods.lithium.common.block.BlockShapeCacheExtended;
import me.jellysquid.mods.lithium.common.block.BlockShapeCacheExtendedProvider;
import me.jellysquid.mods.lithium.common.block.BlockShapeHelper;
import me.jellysquid.mods.lithium.common.util.collections.Object2BooleanCacheTable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Replaces a number of functions in the Block class which are used to determine if some redstone components and other
 * blocks can stand on top of another block. These functions make use of additional cached data in BlockState$ShapeCache.
 */
@Mixin(Block.class)
public class BlockMixin {
    private static final Object2BooleanCacheTable<VoxelShape> FULL_CUBE_CACHE = new Object2BooleanCacheTable<>(
            512,
            shape -> !VoxelShapes.matchesAnywhere(VoxelShapes.fullCube(), shape, BooleanBiFunction.NOT_SAME)
    );

    /**
     * @reason Use the shape cache
     * @author JellySquid
     */
    @Overwrite
    public static boolean hasTopRim(BlockView world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        BlockShapeCacheExtended shapeCache = ((BlockShapeCacheExtendedProvider) state).getExtendedShapeCache();

        if (shapeCache != null) {
            return shapeCache.hasTopRim();
        }

        return hasTopRimFallback(world, pos, state);
    }

    private static boolean hasTopRimFallback(BlockView world, BlockPos pos, BlockState state) {
        return BlockShapeHelper.sideCoversSquare(state.getSidesShape(world, pos).getFace(Direction.UP), BlockShapeHelper.SOLID_MEDIUM_SQUARE_SHAPE);
    }

    /**
     * @reason Use the shape cache
     * @author JellySquid
     */
    @Overwrite
    public static boolean sideCoversSmallSquare(WorldView world, BlockPos pos, Direction side) {
        BlockState state = world.getBlockState(pos);
        BlockShapeCacheExtended shapeCache = ((BlockShapeCacheExtendedProvider) state).getExtendedShapeCache();

        if (shapeCache != null) {
            return shapeCache.sideCoversSmallSquare(side);
        }

        return sideCoversSmallSquareFallback(world, pos, side, state);
    }

    private static boolean sideCoversSmallSquareFallback(WorldView world, BlockPos pos, Direction side, BlockState state) {
        return BlockShapeHelper.sideCoversSquare(state.getSidesShape(world, pos).getFace(side), BlockShapeHelper.SOLID_SMALL_SQUARE_SHAPE);
    }

    /**
     * @reason Use a faster cache implementation
     * @author gegy1000
     */
    @Overwrite
    public static boolean isShapeFullCube(VoxelShape shape) {
        return FULL_CUBE_CACHE.get(shape);
    }
}
