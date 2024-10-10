package net.caffeinemc.mods.lithium.mixin.block.hopper;

import net.caffeinemc.mods.lithium.api.inventory.LithiumInventory;
import net.caffeinemc.mods.lithium.common.hopper.InventoryHelper;
import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.world.inventory.AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    @Inject(method = "getRedstoneSignalFromContainer(Lnet/minecraft/world/Container;)I",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Container;getContainerSize()I", ordinal = 0), cancellable = true)
    private static void getFastOutputStrength(Container inventory, CallbackInfoReturnable<Integer> cir) {
        if (inventory instanceof LithiumInventory optimizedInventory) {
            cir.setReturnValue(InventoryHelper.getLithiumStackList(optimizedInventory).getSignalStrength(inventory));
        }
    }
}
