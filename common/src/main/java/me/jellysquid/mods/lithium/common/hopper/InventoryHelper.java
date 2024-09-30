package me.jellysquid.mods.lithium.common.hopper;

import me.jellysquid.mods.lithium.api.inventory.LithiumInventory;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public class InventoryHelper {
    public static LithiumStackList getLithiumStackList(LithiumInventory inventory) {
        NonNullList<ItemStack> stackList = inventory.getInventoryLithium();
        if (stackList instanceof LithiumStackList lithiumStackList) {
            return lithiumStackList;
        }
        return upgradeToLithiumStackList(inventory);
    }

    public static LithiumStackList getLithiumStackListOrNull(LithiumInventory inventory) {
        NonNullList<ItemStack> stackList = inventory.getInventoryLithium();
        if (stackList instanceof LithiumStackList lithiumStackList) {
            return lithiumStackList;
        }
        return null;
    }

    private static LithiumStackList upgradeToLithiumStackList(LithiumInventory inventory) {
        //generate loot to avoid any problems with directly accessing the inventory slots
        //the loot that is generated here is not generated earlier than in vanilla, because vanilla generates loot
        //when the hopper checks whether the inventory is empty or full
        inventory.generateLootLithium();
        //get the stack list after generating loot, just in case generating loot creates a new stack list
        NonNullList<ItemStack> stackList = inventory.getInventoryLithium();
        LithiumStackList lithiumStackList = new LithiumStackList(stackList, inventory.getMaxStackSize());
        inventory.setInventoryLithium(lithiumStackList);
        return lithiumStackList;
    }
}
