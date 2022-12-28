package me.jellysquid.mods.lithium.mixin.util.block_tracking;

import me.jellysquid.mods.lithium.common.block.BlockCountingSection;
import me.jellysquid.mods.lithium.common.block.BlockStateFlagHolder;
import me.jellysquid.mods.lithium.common.block.BlockStateFlags;
import me.jellysquid.mods.lithium.common.block.TrackedBlockStatePredicate;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Keep track of how many blocks that meet certain criteria are in this chunk section.
 * E.g. if no over-sized blocks are there, collision code can skip a few blocks.
 *
 * @author 2No2Name
 */
@Mixin(ChunkSection.class)
public abstract class ChunkSectionMixin implements BlockCountingSection {

    @Shadow
    @Final
    private PalettedContainer<BlockState> blockStateContainer;
    @Unique
    private short[] countsByFlag = null;
    private CompletableFuture<short[]> countsByFlagFuture;

    @Override
    public boolean anyMatch(TrackedBlockStatePredicate trackedBlockStatePredicate, boolean fallback) {
        if (this.countsByFlag == null) {
            if (!tryInitializeCountsByFlag()) {
                return fallback;
            }
        }
        return this.countsByFlag[trackedBlockStatePredicate.getIndex()] != (short) 0;
    }

    /**
     * Compute the block state counts using a future using a thread pool to avoid lagging the rendering thread.
     * Before modifying the block data, we join the future or discard it.
     *
     * @return Whether the block counts short array is initialized.
     */
    private boolean tryInitializeCountsByFlag() {
        Future<short[]> countsByFlagFuture = this.countsByFlagFuture;
        if (countsByFlagFuture != null && countsByFlagFuture.isDone()) {
            try {
                this.countsByFlag = countsByFlagFuture.get();
                return true;
            } catch (InterruptedException | ExecutionException | CancellationException e) {
                this.countsByFlagFuture = null;
            }
        }

        if (this.countsByFlagFuture == null) {
            PalettedContainer<BlockState> blockStateContainer = this.blockStateContainer;
            this.countsByFlagFuture = CompletableFuture.supplyAsync(() -> calculateLithiumCounts(blockStateContainer));
        }
        return false;
    }

    private static short[] calculateLithiumCounts(PalettedContainer<BlockState> blockStateContainer) {
        short[] countsByFlag = new short[BlockStateFlags.NUM_FLAGS];
        blockStateContainer.count((BlockState state, int count) -> addToFlagCount(countsByFlag, state, count));
        return countsByFlag;
    }

    @Redirect(
            method = "calculateCounts()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/PalettedContainer;count(Lnet/minecraft/world/chunk/PalettedContainer$Counter;)V"
            )
    )
    private void initFlagCounters(PalettedContainer<BlockState> palettedContainer, PalettedContainer.Counter<BlockState> consumer) {
        palettedContainer.count((state, count) -> {
            consumer.accept(state, count);
            addToFlagCount(this.countsByFlag, state, count);
        });
    }

    private static void addToFlagCount(short[] countsByFlag, BlockState state, int change) {
        int flags = ((BlockStateFlagHolder) state).getAllFlags();
        int i;
        while ((i = Integer.numberOfTrailingZeros(flags)) < 32) {
            //either count up by one (prevFlag not set) or down by one (prevFlag set)
            countsByFlag[i] += change;
            flags &= ~(1 << i);
        }
    }

    @Inject(method = "calculateCounts()V", at = @At("HEAD"))
    private void createFlagCounters(CallbackInfo ci) {
        this.countsByFlag = new short[BlockStateFlags.NUM_FLAGS];
    }

    @Inject(
            method = "setBlockState(IIILnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;",
            at = @At(value = "HEAD")
    )
    private void joinFuture(int x, int y, int z, BlockState state, boolean lock, CallbackInfoReturnable<BlockState> cir) {
        if (this.countsByFlagFuture != null) {
            this.countsByFlag = this.countsByFlagFuture.join();
            this.countsByFlagFuture = null;
        }
    }

    @Inject(
            method = "fromPacket",
            at = @At(value = "HEAD")
    )
    private void resetData(PacketByteBuf buf, CallbackInfo ci) {
        this.countsByFlag = null;
        this.countsByFlagFuture = null;
    }


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
    private void updateFlagCounters(int x, int y, int z, BlockState newState, boolean lock, CallbackInfoReturnable<BlockState> cir, BlockState oldState) {
        short[] countsByFlag = this.countsByFlag;
        if (countsByFlag == null) {
            return;
        }
        int prevFlags = ((BlockStateFlagHolder) oldState).getAllFlags();
        int flags = ((BlockStateFlagHolder) newState).getAllFlags();

        //no need to update indices that did not change
        int flagsXOR = prevFlags ^ flags;
        int i;
        while ((i = Integer.numberOfTrailingZeros(flagsXOR)) < 32) {
            //either count up by one (prevFlag not set) or down by one (prevFlag set)
            countsByFlag[i] += 1 - (((prevFlags >>> i) & 1) << 1);
            flagsXOR &= ~(1 << i);
        }
    }
}
