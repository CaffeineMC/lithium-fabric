package me.jellysquid.mods.lithium.mixin.block.hopper;

import me.jellysquid.mods.lithium.api.inventory.LithiumInventory;
import me.jellysquid.mods.lithium.common.hopper.InventoryHelper;
import me.jellysquid.mods.lithium.common.hopper.LithiumDoubleStackList;
import me.jellysquid.mods.lithium.common.hopper.LithiumStackList;
import me.jellysquid.mods.lithium.common.hopper.RemovalCounter;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DoubleInventory.class)
public abstract class DoubleInventoryMixin implements LithiumInventory, RemovalCounter {
    @Shadow
    @Final
    private Inventory first;

    @Shadow
    @Final
    private Inventory second;

    @Shadow
    public abstract int getMaxCountPerStack();

    private LithiumStackList cachedList;

    @Override
    public int getRemovedCountLithium() {
        return ((LithiumInventory) this.first).getRemovedCountLithium() +
                ((LithiumInventory) this.second).getRemovedCountLithium();
    }

    @Override
    public DefaultedList<ItemStack> getInventoryLithium() {
        if (this.cachedList != null) {
            return this.cachedList;
        }
        return this.cachedList = LithiumDoubleStackList.getOrCreate(
                InventoryHelper.getLithiumStackList((LithiumInventory) this.first),
                InventoryHelper.getLithiumStackList((LithiumInventory) this.second),
                this.getMaxCountPerStack()
        );
    }

    @Override
    public void setInventoryLithium(DefaultedList<ItemStack> inventory) {
        throw new UnsupportedOperationException();
    }
}
