package me.jellysquid.mods.lithium.mixin.block.hopper.compat;

import me.jellysquid.mods.lithium.api.inventory.LithiumHopperCompat;
import me.jellysquid.mods.lithium.mixin.block.hopper.CompatHopper;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.function.ToIntBiFunction;

/**
 * This mixin is disabled by default. This mixin exists only for {@link LithiumHopperCompat}.
 */
@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityCompatMixin implements CompatHopper, Hopper {
    @Override
    public boolean mayNotTransferTo(Inventory inventory) {
        ArrayList<ToIntBiFunction<Inventory, Inventory>> itemTransferConditions = LithiumHopperCompat.ITEM_TRANSFER_CONDITIONS;
        for (int i = 0; i < itemTransferConditions.size(); i++) {
            ToIntBiFunction<Inventory, Inventory> transferCondition = itemTransferConditions.get(i);
            if (transferCondition.applyAsInt(this, inventory) == 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mayNotTransferFrom(Inventory inventory) {
        ArrayList<ToIntBiFunction<Inventory, Inventory>> itemTransferConditions = LithiumHopperCompat.ITEM_TRANSFER_CONDITIONS;
        for (int i = 0; i < itemTransferConditions.size(); i++) {
            ToIntBiFunction<Inventory, Inventory> transferCondition = itemTransferConditions.get(i);
            if (transferCondition.applyAsInt(inventory, this) == 0) {
                return true;
            }
        }
        return false;
    }
}
