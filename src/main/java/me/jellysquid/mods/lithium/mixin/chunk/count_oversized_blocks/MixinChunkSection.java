package me.jellysquid.mods.lithium.mixin.chunk.count_oversized_blocks;

import me.jellysquid.mods.lithium.common.entity.movement.ChunkAwareBlockCollisionSweeper;
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
 * Keeps track of how many oversized blocks are in this chunk section. If none are there, collision code can skip a few
 * blocks. Oversized blocks are fences, walls, extended piston heads and blocks with dynamic bounds (scaffolding,
 * shulker box, movable blocks).
 *
 * @author 2No2Name
 */
@Mixin(ChunkSection.class)
public abstract class MixinChunkSection implements ChunkAwareBlockCollisionSweeper.OversizedBlocksCounter {
    @Shadow
    public abstract void calculateCounts();

    @Unique
    private short oversizedBlockCount;

    @Redirect(
            method = "calculateCounts",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/PalettedContainer;count(Lnet/minecraft/world/chunk/PalettedContainer$CountConsumer;)V"
            )
    )
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

    @Inject(
            method = "setBlockState(IIILnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;hasRandomTicks()Z",
                    shift = At.Shift.BEFORE,
                    ordinal = 0,
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void decrementOversizedBlockCount(int x, int y, int z, BlockState state, boolean lock, CallbackInfoReturnable<BlockState> cir) {
        if (state.exceedsCube()) {
            --this.oversizedBlockCount;
        }
    }

    @Inject(
            method = "setBlockState(IIILnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;hasRandomTicks()Z",
                    shift = At.Shift.BEFORE,
                    ordinal = 1
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void incrementOversizedBlockCount(int x, int y, int z, BlockState state, boolean lock, CallbackInfoReturnable<BlockState> cir) {
        if (state.exceedsCube()) {
            ++this.oversizedBlockCount;
        }
    }

    @Override
    public boolean hasOversizedBlocks() {
        return this.oversizedBlockCount > 0;
    }

    /**
     * Initializes oversized block count in client worlds and other values that are not used by client worlds.
     */
    @Environment(EnvType.CLIENT)
    @Inject(method = "fromPacket", at = @At("RETURN"))
    private void initCounts(PacketByteBuf packetByteBuf, CallbackInfo ci) {
        this.calculateCounts();
    }
}
