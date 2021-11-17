package me.jellysquid.mods.lithium.mixin.chunk.block_counting;

import me.jellysquid.mods.lithium.common.block.BlockStateFlagHolder;
import me.jellysquid.mods.lithium.common.block.BlockStateFlags;
import me.jellysquid.mods.lithium.common.block.IndexedBlockStatePredicate;
import me.jellysquid.mods.lithium.common.block.SectionFlagHolder;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Keep track of how many blocks that meet certain criteria are in this chunk section.
 * E.g. if no over-sized blocks are there, collision code can skip a few blocks.
 *
 * @author 2No2Name
 */
@Mixin(ChunkSection.class)
public abstract class ChunkSectionMixin implements SectionFlagHolder {
    @Shadow
    public abstract void calculateCounts();

    @Unique
    private short[] countsByFlag = new short[BlockStateFlags.NUM_FLAGS];

    @Override
    public boolean getFlag(IndexedBlockStatePredicate indexedBlockStatePredicate) {
        return this.countsByFlag[indexedBlockStatePredicate.getIndex()] != 0;
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

            int flags = ((BlockStateFlagHolder) state).getAllFlags();
            int size = this.countsByFlag.length;
            for (int i = 0; i < size && flags != 0; i++) {
                if ((flags & 1) != 0) {
                    this.countsByFlag[i] += count;
                }
                flags = flags >>> 1;
            }
        });
    }

    @Inject(method = "calculateCounts()V", at = @At("HEAD"))
    private void resetFlagCounters(CallbackInfo ci) {
        this.countsByFlag = new short[BlockStateFlags.NUM_FLAGS];
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
        int prevFlags = ((BlockStateFlagHolder) oldState).getAllFlags();
        int flags = ((BlockStateFlagHolder) newState).getAllFlags();

        //no need to update indices that did not change
        int flagsXOR = prevFlags ^ flags;
        int i;
        while ((i = Integer.numberOfTrailingZeros(flagsXOR)) < 32) {
            //either count up by one (prevFlag not set) or down by one (prevFlag set)
            this.countsByFlag[i] += 1 - (((prevFlags >>> i) & 1) << 1);
            flagsXOR &= ~(1 << i);
        }
    }

    /**
     * Initialize the flags in the client worlds.
     * This is required for the oversized block counting collision optimization.
     */
    @Inject(method = "fromPacket(Lnet/minecraft/network/PacketByteBuf;)V", at = @At("RETURN"))
    private void initCounts(PacketByteBuf packetByteBuf, CallbackInfo ci) {
        this.calculateCounts();
    }
}
