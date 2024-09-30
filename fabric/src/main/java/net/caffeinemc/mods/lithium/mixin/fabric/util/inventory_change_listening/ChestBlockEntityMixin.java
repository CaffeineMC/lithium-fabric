package net.caffeinemc.mods.lithium.mixin.fabric.util.inventory_change_listening;

import net.caffeinemc.mods.lithium.common.block.entity.inventory_change_tracking.InventoryChangeEmitter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChestBlockEntity.class, priority = 999)
public abstract class ChestBlockEntityMixin extends RandomizableContainerBlockEntity implements InventoryChangeEmitter {
    protected ChestBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @SuppressWarnings("deprecation")
    @Intrinsic
    @Override
    public void setBlockState(@NotNull BlockState state) {
        super.setBlockState(state);
    }

    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
    @Inject(method = "setBlockState(Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At("RETURN"))
    private void emitRemovedOnSetCachedState(CallbackInfo ci) {
        //Handle switching double / single chest state
        this.lithium$emitRemoved();
    }
}
