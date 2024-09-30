package me.jellysquid.mods.lithium.mixin.util.inventory_change_listening;

import me.jellysquid.mods.lithium.common.block.entity.inventory_change_tracking.InventoryChangeTracker;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class StackListReplacementTracking {

    @Mixin(AbstractFurnaceBlockEntity.class)
    public abstract static class InventoryChangeTrackingAbstractFurnaceBlockEntity implements InventoryChangeTracker {
        //Handled in LockableConainerBlockEntity
    }

    @Mixin(BrewingStandBlockEntity.class)
    public abstract static class InventoryChangeTrackingBrewingStandBlockEntity implements InventoryChangeTracker {
        //Handled in LockableConainerBlockEntity
    }

    @Mixin(BaseContainerBlockEntity.class)
    public abstract static class StackListReplacementTrackingLockableContainerBlockEntity {
        @Inject(method = "loadAdditional", at = @At("RETURN" ))
        public void readNbtStackListReplacement(CompoundTag nbt, HolderLookup.Provider registryLookup, CallbackInfo ci) {
            if (this instanceof InventoryChangeTracker inventoryChangeTracker) {
                inventoryChangeTracker.lithium$emitStackListReplaced();
            }
        }
    }

    @Mixin(BarrelBlockEntity.class)
    public abstract static class InventoryChangeTrackingBarrelBlockEntity implements InventoryChangeTracker {
        @Inject(method = "setItems", at = @At("RETURN"))
        public void setInventoryStackListReplacement(NonNullList<ItemStack> list, CallbackInfo ci) {
            this.lithium$emitStackListReplaced();
        }
    }

    @Mixin(ChestBlockEntity.class)
    public abstract static class InventoryChangeTrackingChestBlockEntity implements InventoryChangeTracker {
        @Inject(method = "setItems", at = @At("RETURN"))
        public void setInventoryStackListReplacement(NonNullList<ItemStack> list, CallbackInfo ci) {
            this.lithium$emitStackListReplaced();
        }
    }

    @Mixin(DispenserBlockEntity.class)
    public abstract static class InventoryChangeTrackingDispenserBlockEntity implements InventoryChangeTracker {
        @Inject(method = "setItems", at = @At("RETURN"))
        public void setInventoryStackListReplacement(NonNullList<ItemStack> list, CallbackInfo ci) {
            this.lithium$emitStackListReplaced();
        }
    }

    @Mixin(HopperBlockEntity.class)
    public abstract static class InventoryChangeTrackingHopperBlockEntity implements InventoryChangeTracker {
        @Inject(method = "setItems", at = @At("RETURN"))
        public void setInventoryStackListReplacement(NonNullList<ItemStack> list, CallbackInfo ci) {
            this.lithium$emitStackListReplaced();
        }
    }

    @Mixin(ShulkerBoxBlockEntity.class)
    public abstract static class InventoryChangeTrackingShulkerBoxBlockEntity implements InventoryChangeTracker {
        @Inject(method = "setItems", at = @At("RETURN"))
        public void setInventoryStackListReplacement(NonNullList<ItemStack> list, CallbackInfo ci) {
            this.lithium$emitStackListReplaced();
        }
    }
}
