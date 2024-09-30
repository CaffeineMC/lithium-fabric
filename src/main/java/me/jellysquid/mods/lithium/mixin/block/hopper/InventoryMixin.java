package me.jellysquid.mods.lithium.mixin.block.hopper;

import me.jellysquid.mods.lithium.api.inventory.LithiumCooldownReceivingInventory;
import me.jellysquid.mods.lithium.api.inventory.LithiumTransferConditionInventory;
import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Container.class)
public interface InventoryMixin extends LithiumCooldownReceivingInventory, LithiumTransferConditionInventory {
}
