package me.jellysquid.mods.lithium.common.world.blockview;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public record SingleBlockBlockView(BlockState state, BlockPos blockPos) implements BlockView {
    public static SingleBlockBlockView of(BlockState blockState, BlockPos blockPos) {
        return new SingleBlockBlockView(blockState, blockPos.toImmutable());
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        throw new SingleBlockViewException();
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (pos.equals(this.blockPos())) {
            return this.state();
        } else {
            throw new SingleBlockViewException();
        }
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        if (pos.equals(this.blockPos())) {
            return this.state().getFluidState();
        } else {
            throw new SingleBlockViewException();
        }
    }

    @Override
    public int getHeight() {
        throw new SingleBlockViewException();
    }

    @Override
    public int getBottomY() {
        throw new SingleBlockViewException();
    }

    public static class SingleBlockViewException extends RuntimeException {

    }
}
