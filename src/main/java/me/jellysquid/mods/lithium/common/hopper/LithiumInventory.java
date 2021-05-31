package me.jellysquid.mods.lithium.common.hopper;

import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

/**
 * Interface to allow accessing the inventory stack lists of inventories and replace them with LithiumStackLists. Any inventory
 * that implements this can benefit from lithium's hopper and inventory optimizations.
 *
 * @author 2No2Name
 */
public interface LithiumInventory extends Inventory {
    DefaultedList<ItemStack> getInventoryLithium();

    void setInventoryLithium(DefaultedList<ItemStack> inventory);

    default int getRemovedCount() { //implemented in BlockEntityMixin
        throw new UnsupportedOperationException();
    }

    default LithiumStackList getLithiumStackList() {
        DefaultedList<ItemStack> stackList = getInventoryLithium();
        if (stackList instanceof LithiumStackList lithiumStackList) {
            return lithiumStackList;
        }
        //generate loot to avoid any problems with directly accessing the inventory slots
        //the loot that is generated here is not generated earlier than in vanilla, because vanilla generates loot
        //when the hopper checks whether the inventory is empty or full
        this.generateLootBeforeConversion();
        LithiumStackList lithiumStackList = new LithiumStackList(stackList, this.getMaxCountPerStack());
        this.setInventoryLithium(lithiumStackList);
        return lithiumStackList;
    }

    default int getSignalStrength() {
        return this.getLithiumStackList().getSignalStrength();
    }

    default void generateLootBeforeConversion() {
        if (this instanceof LootableContainerBlockEntity) {
            ((LootableContainerBlockEntity) this).checkLootInteraction(null);
        }
        if (this instanceof StorageMinecartEntity) {
            ((StorageMinecartEntity) this).generateLoot(null);
        }
    }

}
