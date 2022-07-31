package me.jellysquid.mods.lithium.mixin.block.hopper;

import me.jellysquid.mods.lithium.api.inventory.LithiumInventory;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

public class InventoryAccessors {
    @Mixin(LootableContainerBlockEntity.class)
    public abstract static class InventoryAccessorLootableContainerBlockEntity implements LithiumInventory {
        @Shadow
        protected abstract DefaultedList<ItemStack> getInvStackList();

        @Shadow
        protected abstract void setInvStackList(DefaultedList<ItemStack> list);

        @Override
        public DefaultedList<ItemStack> getInventoryLithium() {
            return this.getInvStackList();
        }

        @Override
        public void setInventoryLithium(DefaultedList<ItemStack> inventory) {
            this.setInvStackList(inventory);
        }
    }

    @Mixin(AbstractFurnaceBlockEntity.class)
    public abstract static class InventoryAccessorAbstractFurnaceBlockEntity implements LithiumInventory {
        @Accessor("inventory" )
        public abstract DefaultedList<ItemStack> getInventoryLithium();

        @Accessor("inventory" )
        public abstract void setInventoryLithium(DefaultedList<ItemStack> inventory);
    }

    @Mixin(BrewingStandBlockEntity.class)
    public abstract static class InventoryAccessorBrewingStandBlockEntity implements LithiumInventory {
        @Override
        @Accessor("inventory" )
        public abstract DefaultedList<ItemStack> getInventoryLithium();

        @Override
        @Accessor("inventory" )
        public abstract void setInventoryLithium(DefaultedList<ItemStack> inventory);
    }

    @Mixin(StorageMinecartEntity.class)
    public abstract static class InventoryAccessorStorageMinecartEntity implements LithiumInventory {
        @Override
        @Accessor("inventory")
        public abstract DefaultedList<ItemStack> getInventoryLithium();

        @Override
        @Accessor("inventory")
        public abstract void setInventoryLithium(DefaultedList<ItemStack> inventory);
    }

}
