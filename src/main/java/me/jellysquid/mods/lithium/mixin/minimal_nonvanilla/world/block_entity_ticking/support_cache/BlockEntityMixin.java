package me.jellysquid.mods.lithium.mixin.minimal_nonvanilla.world.block_entity_ticking.support_cache;

import me.jellysquid.mods.lithium.common.world.blockentity.SupportCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin implements SupportCache {
    @Shadow
    public abstract BlockEntityType<?> getType();

    @Unique
    private boolean supportTestResult;

    @Inject(method = "<init>(Lnet/minecraft/world/level/block/entity/BlockEntityType;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At("RETURN"))
    private void initSupportCache(BlockEntityType<?> type, BlockPos pos, BlockState cachedState, CallbackInfo ci) {
        this.supportTestResult = this.getType().isValid(cachedState);
    }

    @Inject(method = "setBlockState(Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At("RETURN"))
    private void updateSupportCache(BlockState cachedState, CallbackInfo ci) {
        this.supportTestResult = this.getType().isValid(cachedState);
    }

    @Override
    public boolean lithium$isSupported() {
        return this.supportTestResult;
    }
}
