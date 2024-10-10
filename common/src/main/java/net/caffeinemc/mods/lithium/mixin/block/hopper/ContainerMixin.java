package net.caffeinemc.mods.lithium.mixin.block.hopper;

import net.caffeinemc.mods.lithium.api.inventory.LithiumCooldownReceivingInventory;
import net.caffeinemc.mods.lithium.api.inventory.LithiumTransferConditionInventory;
import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Container.class)
public interface ContainerMixin extends LithiumCooldownReceivingInventory, LithiumTransferConditionInventory {
}
