package net.caffeinemc.mods.lithium.mixin.util.inventory_change_listening;

import net.caffeinemc.mods.lithium.common.block.entity.inventory_change_tracking.InventoryChangeTracker;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class StackListReplacementTracking {

    @Mixin(AbstractFurnaceBlockEntity.class)
    public abstract static class AbstractFurnaceBlockEntityMixin implements InventoryChangeTracker {
        //Handled in LockableConainerBlockEntity
    }

    @Mixin(BrewingStandBlockEntity.class)
    public abstract static class BrewingStandBlockEntityMixin implements InventoryChangeTracker {
        //Handled in LockableConainerBlockEntity
    }

    @Mixin(BaseContainerBlockEntity.class)
    public abstract static class BaseContainerBlockEntityMixin {
        @Inject(method = "loadAdditional", at = @At("RETURN"))
        public void readNbtStackListReplacement(CompoundTag nbt, HolderLookup.Provider registryLookup, CallbackInfo ci) {
            if (this instanceof InventoryChangeTracker inventoryChangeTracker) {
                inventoryChangeTracker.lithium$emitStackListReplaced();
            }
        }
    }

    @Mixin(BarrelBlockEntity.class)
    public abstract static class BarrelBlockEntityMixin implements InventoryChangeTracker {
        @Inject(method = "setItems", at = @At("RETURN"))
        public void setInventoryStackListReplacement(NonNullList<ItemStack> list, CallbackInfo ci) {
            this.lithium$emitStackListReplaced();
        }
    }

    @Mixin(ChestBlockEntity.class)
    public abstract static class ChestBlockEntityMixin implements InventoryChangeTracker {
        @Inject(method = "setItems", at = @At("RETURN"))
        public void setInventoryStackListReplacement(NonNullList<ItemStack> list, CallbackInfo ci) {
            this.lithium$emitStackListReplaced();
        }
    }

    @Mixin(DispenserBlockEntity.class)
    public abstract static class DispenserBlockEntityMixin implements InventoryChangeTracker {
        @Inject(method = "setItems", at = @At("RETURN"))
        public void setInventoryStackListReplacement(NonNullList<ItemStack> list, CallbackInfo ci) {
            this.lithium$emitStackListReplaced();
        }
    }

    @Mixin(HopperBlockEntity.class)
    public abstract static class HopperBlockEntityMixin implements InventoryChangeTracker {
        @Inject(method = "setItems", at = @At("RETURN"))
        public void setInventoryStackListReplacement(NonNullList<ItemStack> list, CallbackInfo ci) {
            this.lithium$emitStackListReplaced();
        }
    }

    @Mixin(ShulkerBoxBlockEntity.class)
    public abstract static class ShulkerBoxBlockEntityMixin implements InventoryChangeTracker {
        @Inject(method = "setItems", at = @At("RETURN"))
        public void setInventoryStackListReplacement(NonNullList<ItemStack> list, CallbackInfo ci) {
            this.lithium$emitStackListReplaced();
        }
    }
}
