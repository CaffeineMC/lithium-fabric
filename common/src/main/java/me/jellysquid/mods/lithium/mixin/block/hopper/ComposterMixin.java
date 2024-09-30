package me.jellysquid.mods.lithium.mixin.block.hopper;

import me.jellysquid.mods.lithium.common.hopper.BlockStateOnlyInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class ComposterMixin {

    @Mixin(targets = "net/minecraft/world/level/block/ComposterBlock$InputContainer")
    static abstract class ComposterBlockComposterInventoryMixin implements BlockStateOnlyInventory {
        @Shadow
        private boolean changed;

        /**
         * Fixes composter inventories becoming blocked forever for no reason, which makes them not cacheable.
         */
        @Inject(
                method = "setChanged",
                at = @At(
                        value = "INVOKE",
                        target = "Lnet/minecraft/world/level/block/ComposterBlock$InputContainer;removeItemNoUpdate(I)Lnet/minecraft/world/item/ItemStack;"
                )
        )
        private void resetDirty(CallbackInfo ci) {
            this.changed = false;
        }

    }

    @Mixin(targets = "net/minecraft/world/level/block/ComposterBlock$EmptyContainer")
    static abstract class ComposterBlockDummyInventoryMixin implements BlockStateOnlyInventory {

    }

    @Mixin(targets = "net/minecraft/world/level/block/ComposterBlock$OutputContainer")
    static abstract class ComposterBlockFullComposterInventoryMixin implements BlockStateOnlyInventory {

    }
}
