package me.jellysquid.mods.lithium.common.block.entity.inventory_comparator_tracking;

import me.jellysquid.mods.lithium.common.util.DirectionConstants;
import me.jellysquid.mods.lithium.common.world.blockentity.BlockEntityGetter;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ComparatorTracking {

    public static void notifyNearbyBlockEntitiesAboutNewComparator(World world, BlockPos pos) {
        BlockPos.Mutable searchPos = new BlockPos.Mutable();
        for (Direction searchDirection : DirectionConstants.HORIZONTAL) {
            for (int searchOffset = 1; searchOffset <= 2; searchOffset++) {
                searchPos.set(pos);
                searchPos.move(searchDirection, searchOffset);
                BlockState blockState = world.getBlockState(searchPos);
                if (blockState.getBlock() instanceof BlockEntityProvider) {
                    BlockEntity loadedExistingBlockEntity = ((BlockEntityGetter) world).getLoadedExistingBlockEntity(searchPos);
                    if (loadedExistingBlockEntity instanceof Inventory) {
                        ((ComparatorTracker) loadedExistingBlockEntity).onComparatorAdded(searchDirection, searchOffset);
                    }
                }
            }
        }
    }

    public static byte findNearbyComparators(World world, BlockPos pos) {
        byte comparatorsNearby = 0;
        BlockPos.Mutable searchPos = new BlockPos.Mutable();
        for (Direction searchDirection : DirectionConstants.HORIZONTAL) {
            for (int searchOffset = 1; searchOffset <= 2; searchOffset++) {
                searchPos.set(pos);
                searchPos.move(searchDirection, searchOffset);
                BlockState blockState = world.getBlockState(searchPos);
                if (blockState.isOf(Blocks.COMPARATOR)) {
                    comparatorsNearby = (byte) (comparatorsNearby | (1 << (searchDirection.getId() * 2 + (searchOffset - 1))));
                }
            }
        }
        return comparatorsNearby;
    }
}
