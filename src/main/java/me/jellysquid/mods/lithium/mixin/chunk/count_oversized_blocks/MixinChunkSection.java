package me.jellysquid.mods.lithium.mixin.chunk.count_oversized_blocks;

import me.jellysquid.mods.lithium.common.entity.movement.ChunkAwareBlockCollisionSweeper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
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
 * Keep track of how many oversized blocks are in this chunk section. If none are there, collision code can skip a few blocks.
 * Oversized blocks are fences, walls, extended piston heads and blocks with dynamic bounds (scaffolding, shulker box, moving blocks)
 * @author 2No2Name
 */
@Mixin(ChunkSection.class)
public abstract class MixinChunkSection implements ChunkAwareBlockCollisionSweeper.OversizedBlocksCounter {
    @Shadow
    public abstract void calculateCounts();

    @Unique
    private short oversizedBlockCount;

    @Redirect(method = "calculateCounts", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/PalettedContainer;count(Lnet/minecraft/world/chunk/PalettedContainer$CountConsumer;)V"))
    private void addToOversizedBlockCount(PalettedContainer<BlockState> palettedContainer, PalettedContainer.CountConsumer<BlockState> consumer) {
        palettedContainer.count((state, count) -> {
            consumer.accept(state, count);
            if (state.exceedsCube()) {
                this.oversizedBlockCount += count;
            }
        });
    }

    @Inject(method = "calculateCounts", at = @At("HEAD"))
    private void resetOversizedBlockCount(CallbackInfo ci) {
        this.oversizedBlockCount = 0;
    }

    @Inject(method = "setBlockState(IIILnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;", at = @At(ordinal = 0, value = "INVOKE", target = "Lnet/minecraft/block/BlockState;hasRandomTicks()Z", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    private void decrOversizedBlockCount(int x, int y, int z, BlockState state, boolean lock, CallbackInfoReturnable<BlockState> cir, BlockState blockState2, FluidState fluidState, FluidState fluidState2) {
        if (blockState2.exceedsCube()) {
            --this.oversizedBlockCount;
        }
    }

    @Inject(method = "setBlockState(IIILnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;", at = @At(ordinal = 1, value = "INVOKE", target = "Lnet/minecraft/block/BlockState;hasRandomTicks()Z", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    private void incrOversizedBlockCount(int x, int y, int z, BlockState state, boolean lock, CallbackInfoReturnable<BlockState> cir) {
        if (state.exceedsCube()) {
            ++this.oversizedBlockCount;
        }
    }

    @Override
    public boolean hasOversizedBlocks() {
        return this.oversizedBlockCount > 0;
    }

    /**
     * Initialize oversized block count in the client worlds.
     * This also initializes other values (randomtickable blocks counter), but they are unused in the client worlds.
     */
    @Environment(EnvType.CLIENT)
    @Inject(method = "fromPacket", at = @At("RETURN"))
    private void initCounts(PacketByteBuf packetByteBuf, CallbackInfo ci) {
        this.calculateCounts();
    }
}
