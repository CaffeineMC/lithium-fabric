package me.jellysquid.mods.lithium.mixin.voxelshape.fast_shape_comparisons;

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

@Mixin(Block.class)
public class MixinBlock {
    @Shadow
    @Final
    private static VoxelShape SOLID_MEDIUM_SQUARE_SHAPE;

    @Shadow
    @Final
    private static VoxelShape SOLID_SMALL_SQUARE_SHAPE;

    /**
     * @reason Avoid the expensive call to VoxelShapes#matchesAnywhere if the block in question is a full cube
     * @author JellySquid
     */
    @Overwrite
    public static boolean topCoversMediumSquare(BlockView world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        if (state.matches(BlockTags.LEAVES)) {
            return false;
        }

        VoxelShape shape = state.getCollisionShape(world, pos).getFace(Direction.UP);

        return shape == VoxelShapes.fullCube() || !VoxelShapes.matchesAnywhere(shape, SOLID_MEDIUM_SQUARE_SHAPE, BooleanBiFunction.ONLY_SECOND);
    }


    /**
     * @reason Avoid the expensive call to VoxelShapes#matchesAnywhere if the block in question is a full cube
     * @author JellySquid
     */
    @Overwrite
    public static boolean sideCoversSmallSquare(WorldView world, BlockPos pos, Direction side) {
        BlockState state = world.getBlockState(pos);

        if (state.matches(BlockTags.LEAVES)) {
            return false;
        }

        VoxelShape shape = state.getCollisionShape(world, pos).getFace(side);

        return shape == VoxelShapes.fullCube() || !VoxelShapes.matchesAnywhere(shape, SOLID_SMALL_SQUARE_SHAPE, BooleanBiFunction.ONLY_SECOND);
    }

}
