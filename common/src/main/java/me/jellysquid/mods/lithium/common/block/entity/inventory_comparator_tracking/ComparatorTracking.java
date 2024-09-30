package me.jellysquid.mods.lithium.common.block.entity.inventory_comparator_tracking;

import me.jellysquid.mods.lithium.common.util.DirectionConstants;
import me.jellysquid.mods.lithium.common.world.blockentity.BlockEntityGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ComparatorTracking {

    public static void notifyNearbyBlockEntitiesAboutNewComparator(Level world, BlockPos pos) {
        BlockPos.MutableBlockPos searchPos = new BlockPos.MutableBlockPos();
        for (Direction searchDirection : DirectionConstants.HORIZONTAL) {
            for (int searchOffset = 1; searchOffset <= 2; searchOffset++) {
                searchPos.set(pos);
                searchPos.move(searchDirection, searchOffset);
                BlockState blockState = world.getBlockState(searchPos);
                if (blockState.getBlock() instanceof EntityBlock) {
                    BlockEntity blockEntity = ((BlockEntityGetter) world).lithium$getLoadedExistingBlockEntity(searchPos);
                    if (blockEntity instanceof Container && blockEntity instanceof ComparatorTracker comparatorTracker) {
                        comparatorTracker.lithium$onComparatorAdded(searchDirection, searchOffset);
                    }
                }
            }
        }
    }

    public static boolean findNearbyComparators(Level world, BlockPos pos) {
        BlockPos.MutableBlockPos searchPos = new BlockPos.MutableBlockPos();
        for (Direction searchDirection : DirectionConstants.HORIZONTAL) {
            for (int searchOffset = 1; searchOffset <= 2; searchOffset++) {
                searchPos.set(pos);
                searchPos.move(searchDirection, searchOffset);
                BlockState blockState = world.getBlockState(searchPos);
                if (blockState.is(Blocks.COMPARATOR)) {
                    return true;
                }
            }
        }
        return false;
    }
}
