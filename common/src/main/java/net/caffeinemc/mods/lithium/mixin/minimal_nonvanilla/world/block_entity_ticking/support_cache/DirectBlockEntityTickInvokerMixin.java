package net.caffeinemc.mods.lithium.mixin.minimal_nonvanilla.world.block_entity_ticking.support_cache;

import net.caffeinemc.mods.lithium.common.world.blockentity.SupportCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(targets = "net/minecraft/world/level/chunk/LevelChunk$BoundTickingBlockEntity")
public class DirectBlockEntityTickInvokerMixin<T extends BlockEntity> {

    @Shadow
    @Final
    private T blockEntity;

    @Redirect(
            method = "tick()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/LevelChunk;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
            ),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/util/function/Supplier;)V"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntityTicker;tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntity;)V")
            )
    )
    private BlockState getCachedState(LevelChunk chunk, BlockPos pos) {
        return this.blockEntity.getBlockState();
    }

    @Redirect(
            method = "tick()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/BlockEntityType;isValid(Lnet/minecraft/world/level/block/state/BlockState;)Z"
            ),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/util/function/Supplier;)V"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntityTicker;tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntity;)V")
            )
    )
    private boolean cachedIsSupported(BlockEntityType<?> blockEntityType, BlockState block) {
        return ((SupportCache) this.blockEntity).lithium$isSupported();
    }

}
