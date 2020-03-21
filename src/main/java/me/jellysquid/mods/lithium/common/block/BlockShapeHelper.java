package me.jellysquid.mods.lithium.common.block;

import net.minecraft.block.Block;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class BlockShapeHelper {
    public static final VoxelShape SOLID_MEDIUM_SQUARE_SHAPE = VoxelShapes.combineAndSimplify(VoxelShapes.fullCube(), Block.createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D), BooleanBiFunction.ONLY_FIRST);
    public static final VoxelShape SOLID_SMALL_SQUARE_SHAPE = Block.createCuboidShape(7.0D, 0.0D, 7.0D, 9.0D, 10.0D, 9.0D);

    public static boolean sideCoversSquare(VoxelShape face, VoxelShape square) {
        // Avoid the expensive call to VoxelShapes#matchesAnywhere if the block in question is a full cube
        // A full cube can always cover a square on any side
        if (face == VoxelShapes.fullCube()) {
            return true;
        }

        return !VoxelShapes.matchesAnywhere(face, square, BooleanBiFunction.ONLY_SECOND);
    }
}
