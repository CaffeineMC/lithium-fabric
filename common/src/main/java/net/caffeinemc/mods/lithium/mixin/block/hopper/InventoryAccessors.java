package net.caffeinemc.mods.lithium.mixin.block.hopper;

import net.caffeinemc.mods.lithium.api.inventory.LithiumInventory;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

public class InventoryAccessors {
    @Mixin(AbstractFurnaceBlockEntity.class)
    public abstract static class AbstractFurnaceBlockEntityMixin implements LithiumInventory {
        @Accessor("items")
        public abstract NonNullList<ItemStack> getInventoryLithium();

        @Accessor("items")
        public abstract void setInventoryLithium(NonNullList<ItemStack> inventory);
    }

    @Mixin(BarrelBlockEntity.class)
    public abstract static class BarrelBlockEntityMixin implements LithiumInventory {
        @Override
        @Accessor("items")
        public abstract NonNullList<ItemStack> getInventoryLithium();

        @Override
        @Accessor("items")
        public abstract void setInventoryLithium(NonNullList<ItemStack> inventory);
    }

    @Mixin(BrewingStandBlockEntity.class)
    public abstract static class BrewingStandBlockEntityMixin implements LithiumInventory {
        @Override
        @Accessor("items")
        public abstract NonNullList<ItemStack> getInventoryLithium();

        @Override
        @Accessor("items")
        public abstract void setInventoryLithium(NonNullList<ItemStack> inventory);
    }

    @Mixin(ChestBlockEntity.class)
    public abstract static class ChestBlockEntityMixin implements LithiumInventory {
        @Override
        @Accessor("items")
        public abstract NonNullList<ItemStack> getInventoryLithium();

        @Override
        @Accessor("items")
        public abstract void setInventoryLithium(NonNullList<ItemStack> inventory);
    }

    @Mixin(DispenserBlockEntity.class)
    public abstract static class DispenserBlockEntityMixin implements LithiumInventory {
        @Override
        @Accessor("items")
        public abstract NonNullList<ItemStack> getInventoryLithium();

        @Override
        @Accessor("items")
        public abstract void setInventoryLithium(NonNullList<ItemStack> inventory);
    }

    @Mixin(HopperBlockEntity.class)
    public abstract static class HopperBlockEntityMixin implements LithiumInventory {
        @Override
        @Accessor("items")
        public abstract NonNullList<ItemStack> getInventoryLithium();

        @Override
        @Accessor("items")
        public abstract void setInventoryLithium(NonNullList<ItemStack> inventory);
    }

    @Mixin(ShulkerBoxBlockEntity.class)
    public abstract static class ShulkerBoxBlockEntityMixin implements LithiumInventory {
        @Override
        @Accessor("itemStacks")
        public abstract NonNullList<ItemStack> getInventoryLithium();

        @Override
        @Accessor("itemStacks")
        public abstract void setInventoryLithium(NonNullList<ItemStack> inventory);
    }

    @Mixin(AbstractMinecartContainer.class)
    public abstract static class AbstractMinecartContainerMixin implements LithiumInventory {
        @Override
        @Accessor("itemStacks")
        public abstract NonNullList<ItemStack> getInventoryLithium();

        @Override
        @Accessor("itemStacks")
        public abstract void setInventoryLithium(NonNullList<ItemStack> inventory);
    }

}
