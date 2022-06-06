package me.jellysquid.mods.lithium.mixin.block.hopper;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.jellysquid.mods.lithium.common.hopper.LithiumStackList;
import me.jellysquid.mods.lithium.common.hopper.StorableItemStack;
import me.jellysquid.mods.lithium.common.util.tuples.RefIntPair;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Set;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements StorableItemStack {

    @Shadow
    private int count;

    private int mySlot;

    @Nullable
    private Object myLocation;

    @Override
    public void registerToInventory(LithiumStackList itemStacks, int mySlot) {
        if (this.myLocation != null) {
            this.lithiumRegisterMultipleInventories(itemStacks, mySlot);
        } else {
            this.myLocation = itemStacks;
            this.mySlot = mySlot;
        }
    }

    @Override
    public void unregisterFromInventory(LithiumStackList stackList) {
        this.unregisterFromInventory(stackList, -1);
    }

    @Override
    public void unregisterFromInventory(LithiumStackList myInventoryList, int index) {
        if (this.myLocation == myInventoryList) {
            this.myLocation = null;
            this.mySlot = -1;
        } else if (this.myLocation instanceof Set<?>) {
            this.lithiumUnregisterMultipleInventories(myInventoryList, index);
        } else {
            //Todo does this even happen? This seems to be unexpected behavior
            this.myLocation = null;
        }
    }

    @ModifyVariable(method = "setCount(I)V", at = @At("HEAD"))
    public int updateInventory(int count) {
        if (this.myLocation != null && this.count != count) {
            if (this.myLocation instanceof LithiumStackList stackList) {
                stackList.beforeSlotCountChange(this.mySlot, count);
            } else {
                this.lithiumUpdateMultipleInventories();
            }
        }
        return count;
    }

    private void lithiumRegisterMultipleInventories(LithiumStackList itemStacks, int mySlot) {
        Set<RefIntPair<LithiumStackList>> stackLists;
        if (this.myLocation instanceof Set<?>) {
            //noinspection unchecked
            stackLists = (Set<RefIntPair<LithiumStackList>>) this.myLocation;
        } else {
            stackLists = new ObjectOpenHashSet<>();
            if (this.myLocation != null) {
                RefIntPair<LithiumStackList> pair = new RefIntPair<>((LithiumStackList) this.myLocation, this.mySlot);
                stackLists.add(pair);
                this.myLocation = stackLists;
                this.mySlot = -1;
            }
        }
        RefIntPair<LithiumStackList> pair = new RefIntPair<>(itemStacks, mySlot);
        stackLists.add(pair);
    }

    private void lithiumUnregisterMultipleInventories(LithiumStackList itemStacks, int mySlot) {
        //Handle shadow item technology correctly (Item in multiple inventories at once!)
        if (this.myLocation instanceof Set<?> set) {
            //noinspection unchecked
            Set<RefIntPair<LithiumStackList>> stackLists = (Set<RefIntPair<LithiumStackList>>) set;
            if (mySlot >= 0) {
                stackLists.remove(new RefIntPair<>(itemStacks, mySlot));
            } else {
                stackLists.removeIf(stackListSlotPair -> stackListSlotPair.left() == itemStacks);
            }

        }
    }

    private void lithiumUpdateMultipleInventories() {
        //Handle shadow item technology correctly (Item in multiple inventories at once!)
        if (this.myLocation instanceof Set<?> set) {
            //noinspection unchecked
            Set<RefIntPair<LithiumStackList>> stackLists = (Set<RefIntPair<LithiumStackList>>) set;
            for (RefIntPair<LithiumStackList> stackListLocationPair : stackLists) {
                stackListLocationPair.left().beforeSlotCountChange(stackListLocationPair.right(), count);
            }

        }
    }
}
