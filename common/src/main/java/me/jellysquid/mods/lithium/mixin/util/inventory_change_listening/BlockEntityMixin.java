package me.jellysquid.mods.lithium.mixin.util.inventory_change_listening;

import me.jellysquid.mods.lithium.common.block.entity.inventory_change_tracking.InventoryChangeTracker;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public class BlockEntityMixin {

    @Shadow
    @Nullable
    protected Level level;

    @Inject(
            method = "setRemoved()V",
            at = @At("RETURN")
    )
    private void updateStackListTracking(CallbackInfo ci) {
        if (this.level != null && !this.level.isClientSide() && this instanceof InventoryChangeTracker inventoryChangeTracker) {
            inventoryChangeTracker.lithium$emitRemoved();
        }
    }
}
