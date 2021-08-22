package me.jellysquid.mods.lithium.mixin.chunk.block_change_counting;

import me.jellysquid.mods.lithium.common.block.SectionModCounter;
import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ChunkSection.class)
public class ChunkSectionMixin implements SectionModCounter {
    private long modCount;

    @Inject(
            method = "setBlockState(IIILnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;getFluidState()Lnet/minecraft/fluid/FluidState;",
                    ordinal = 0,
                    shift = At.Shift.BEFORE
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void updateChangeCount(int x, int y, int z, BlockState newState, boolean lock, CallbackInfoReturnable<BlockState> cir, BlockState oldState) {
        if (newState != oldState) {
            long modCount = this.modCount;
            if (modCount < Long.MAX_VALUE) {
                this.modCount = modCount + 1;
            }
        }
    }


    @Override
    public boolean isUnchanged(long modCount) {
        return this.modCount == modCount && modCount < Long.MAX_VALUE;
    }

    @Override
    public long getModCount() {
        return this.modCount;
    }
}
