package me.jellysquid.mods.lithium.mixin.chunk.count_oversized_blocks;

import me.jellysquid.mods.lithium.common.block.BlockStateFlags;
import me.jellysquid.mods.lithium.common.block.Flag;
import me.jellysquid.mods.lithium.common.block.FlagHolder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
public abstract class ChunkSectionMixin implements FlagHolder {
    @Shadow
    public abstract void calculateCounts();

    @Unique
    private short[] countsByFlag = new short[BlockStateFlags.NUM_FLAGS];

    @Override
    public boolean getFlag(Flag.CachedFlag cachedFlag) {
        return this.countsByFlag[cachedFlag.getIndex()] != 0;
    }

    @Override
    public int getAllFlags() {
        int flags = 0;
        int size = this.countsByFlag.length;
        for (int i = 0; i < size; i++) {
            if (this.countsByFlag[i] != 0) {
                flags |= 1 << i;
            }
        }
        return flags;
    }

    @Redirect(
            method = "calculateCounts",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/PalettedContainer;count(Lnet/minecraft/world/chunk/PalettedContainer$CountConsumer;)V"
            )
    )
    private void initFlagCounters(PalettedContainer<BlockState> palettedContainer, PalettedContainer.CountConsumer<BlockState> consumer) {
        palettedContainer.count((state, count) -> {
            consumer.accept(state, count);

            int flags = ((FlagHolder) state).getAllFlags();
            int size = this.countsByFlag.length;
            for (int i = 0; i < size && flags != 0; i++) {
                if ((flags & 1) != 0) {
                    this.countsByFlag[i] += count;
                }
                flags = flags >>> 1;
            }
        });
    }

    @Inject(method = "calculateCounts", at = @At("HEAD"))
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
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    private void updateFlagCounters(int x, int y, int z, BlockState state, boolean lock, CallbackInfoReturnable<BlockState> cir, BlockState blockState2) {
        int prevFlags = ((FlagHolder) blockState2).getAllFlags();
        int flags = ((FlagHolder) state).getAllFlags();

        //no need to update indices that did not change
        int flagsXOR = prevFlags ^ flags;
        prevFlags &= flagsXOR;
        flags &= flagsXOR;

        int size = this.countsByFlag.length;
        int mask = 1;
        for (int i = 0; i < size && flags != prevFlags; i++) {
            if ((prevFlags & mask) != 0) {
                this.countsByFlag[i]--;
            }
            if ((flags & mask) != 0) {
                this.countsByFlag[i]++;
            }
            mask = mask << 1;
        }
    }

    /**
     * Initialize the flags in the client worlds.
     * This is required for the oversized block counting collision optimization.
     */
    @Environment(EnvType.CLIENT)
    @Inject(method = "fromPacket", at = @At("RETURN"))
    private void initCounts(PacketByteBuf packetByteBuf, CallbackInfo ci) {
        this.calculateCounts();
    }
}
