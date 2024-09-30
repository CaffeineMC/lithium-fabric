package me.jellysquid.mods.lithium.mixin.block.hopper;

import me.jellysquid.mods.lithium.api.inventory.LithiumInventory;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

public class InventoryAccessors {
    @Mixin(AbstractFurnaceBlockEntity.class)
    public abstract static class InventoryAccessorAbstractFurnaceBlockEntity implements LithiumInventory {
        @Accessor("items")
        public abstract NonNullList<ItemStack> getInventoryLithium();

        @Accessor("items")
        public abstract void setInventoryLithium(NonNullList<ItemStack> inventory);
    }

    @Mixin(BarrelBlockEntity.class)
    public abstract static class InventoryAccessorBarrelBlockEntity implements LithiumInventory {
        @Override
        @Accessor("items")
        public abstract NonNullList<ItemStack> getInventoryLithium();

        @Override
        @Accessor("items")
        public abstract void setInventoryLithium(NonNullList<ItemStack> inventory);
    }

    @Mixin(BrewingStandBlockEntity.class)
    public abstract static class InventoryAccessorBrewingStandBlockEntity implements LithiumInventory {
        @Override
        @Accessor("items")
        public abstract NonNullList<ItemStack> getInventoryLithium();

        @Override
        @Accessor("items")
        public abstract void setInventoryLithium(NonNullList<ItemStack> inventory);
    }

    @Mixin(ChestBlockEntity.class)
    public abstract static class InventoryAccessorChestBlockEntity implements LithiumInventory {
        @Override
        @Accessor("items")
        public abstract NonNullList<ItemStack> getInventoryLithium();

        @Override
        @Accessor("items")
        public abstract void setInventoryLithium(NonNullList<ItemStack> inventory);
    }

    @Mixin(DispenserBlockEntity.class)
    public abstract static class InventoryAccessorDispenserBlockEntity implements LithiumInventory {
        @Override
        @Accessor("items")
        public abstract NonNullList<ItemStack> getInventoryLithium();

        @Override
        @Accessor("items")
        public abstract void setInventoryLithium(NonNullList<ItemStack> inventory);
    }

    @Mixin(HopperBlockEntity.class)
    public abstract static class InventoryAccessorHopperBlockEntity implements LithiumInventory {
        @Override
        @Accessor("items")
        public abstract NonNullList<ItemStack> getInventoryLithium();

        @Override
        @Accessor("items")
        public abstract void setInventoryLithium(NonNullList<ItemStack> inventory);
    }

    @Mixin(ShulkerBoxBlockEntity.class)
    public abstract static class InventoryAccessorShulkerBoxBlockEntity implements LithiumInventory {
        @Override
        @Accessor("itemStacks")
        public abstract NonNullList<ItemStack> getInventoryLithium();

        @Override
        @Accessor("itemStacks")
        public abstract void setInventoryLithium(NonNullList<ItemStack> inventory);
    }

    @Mixin(AbstractMinecartContainer.class)
    public abstract static class InventoryAccessorStorageMinecartEntity implements LithiumInventory {
        @Override
        @Accessor("itemStacks")
        public abstract NonNullList<ItemStack> getInventoryLithium();

        @Override
        @Accessor("itemStacks")
        public abstract void setInventoryLithium(NonNullList<ItemStack> inventory);
    }

}
