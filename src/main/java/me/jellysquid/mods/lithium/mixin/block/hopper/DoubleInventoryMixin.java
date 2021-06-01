package me.jellysquid.mods.lithium.mixin.block.hopper;

import me.jellysquid.mods.lithium.common.hopper.LithiumDoubleStackList;
import me.jellysquid.mods.lithium.common.hopper.LithiumInventory;
import me.jellysquid.mods.lithium.common.hopper.LithiumStackList;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DoubleInventory.class)
public abstract class DoubleInventoryMixin implements LithiumInventory {
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
    public LithiumStackList getLithiumStackList() {
        if (this.cachedList != null) {
            return this.cachedList;
        }
        return this.cachedList = LithiumDoubleStackList.getOrCreate(
                ((LithiumInventory) this.first).getLithiumStackList(),
                ((LithiumInventory) this.second).getLithiumStackList(),
                this.getMaxCountPerStack()
        );
    }

    @Override
    public int getRemovedCount() {
        return ((LithiumInventory) this.first).getRemovedCount() +
                ((LithiumInventory) this.second).getRemovedCount();
    }

    @Override
    public int getSignalStrength() {
        return this.getLithiumStackList().getSignalStrength();
    }

    @Override
    public DefaultedList<ItemStack> getInventoryLithium() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setInventoryLithium(DefaultedList<ItemStack> inventory) {
        throw new UnsupportedOperationException();
    }
}
