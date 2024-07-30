package me.jellysquid.mods.lithium.common.hopper;

import me.jellysquid.mods.lithium.api.inventory.LithiumTransferConditionInventory;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class HopperHelper {

    public static boolean tryMoveSingleItem(Inventory to, ItemStack stack, @Nullable Direction fromDirection) {
        ItemStack transferChecker;
        if (((LithiumTransferConditionInventory) to).lithium$itemInsertionTestRequiresStackSize1()) {
            transferChecker = stack.copy();
            transferChecker.setCount(1);
        } else {
            transferChecker = stack;
        }

        SidedInventory toSided = to instanceof SidedInventory ? ((SidedInventory) to) : null;
        if (toSided != null && fromDirection != null) {
            int[] slots = toSided.getAvailableSlots(fromDirection);

            for(int slotIndex = 0; slotIndex < slots.length; ++slotIndex) {
                if (tryMoveSingleItem(to, toSided, stack, transferChecker, slots[slotIndex], fromDirection)) {
                    return true; //caller needs to take the item from the original inventory and call to.markDirty()
                }
            }
        } else {
            int j = to.size();
            for(int slot = 0; slot < j; ++slot) {
                if (tryMoveSingleItem(to, toSided, stack, transferChecker, slot, fromDirection)) {
                    return true; //caller needs to take the item from the original inventory and call to.markDirty()
                }
            }
        }
        return false;
    }

    public static boolean tryMoveSingleItem(Inventory to, @Nullable SidedInventory toSided, ItemStack transferStack, ItemStack transferChecker, int targetSlot, @Nullable Direction fromDirection) {
        ItemStack toStack = to.getStack(targetSlot);
        if (to.isValid(targetSlot, transferChecker) && (toSided == null || toSided.canInsert(targetSlot, transferChecker, fromDirection))) {
            int toCount;
            if (toStack.isEmpty()) {
                ItemStack singleItem = transferStack.split(1);
                to.setStack(targetSlot, singleItem);
                return true; //caller needs to call to.markDirty()
            } else if (toStack.getMaxCount() > (toCount = toStack.getCount()) && to.getMaxCountPerStack() > toCount && ItemStack.areItemsAndComponentsEqual(toStack, transferStack)) {
                transferStack.decrement(1);
                toStack.increment(1);
                return true; //caller needs to call to.markDirty()
            }
        }
        return false;
    }

    private static int calculateReducedSignalStrength(float contentWeight, int inventorySize, int inventoryMaxCountPerStack, int numOccupiedSlots, int itemStackCount, int itemStackMaxCount) {
        //contentWeight adaption can include rounding error for non-power of 2 max stack sizes, which do not exist in vanilla anyways
        int maxStackSize = Math.min(inventoryMaxCountPerStack, itemStackMaxCount);
        int newNumOccupiedSlots = numOccupiedSlots - (itemStackCount == 1 ? 1 : 0);
        float newContentWeight = contentWeight - (1f / (float) maxStackSize);
        newContentWeight /= (float) inventorySize;
        return MathHelper.floor(newContentWeight * 14.0F) + (newNumOccupiedSlots > 0 ? 1 : 0);
    }

    public static ComparatorUpdatePattern determineComparatorUpdatePattern(Inventory from, LithiumStackList fromStackList) {
        if ((from instanceof HopperBlockEntity) || !(from instanceof LootableContainerBlockEntity)) {
            return ComparatorUpdatePattern.NO_UPDATE;
        }
        //calculate the signal strength of the inventory, but also keep the content weight variable
        float contentWeight = 0f;
        int numOccupiedSlots = 0;

        for (int j = 0; j < from.size(); ++j) {
            ItemStack itemStack = from.getStack(j);
            if (!itemStack.isEmpty()) {
                int maxStackSize = Math.min(from.getMaxCountPerStack(), itemStack.getMaxCount());
                contentWeight += itemStack.getCount() / (float) maxStackSize;
                ++numOccupiedSlots;
            }
        }
        float f = contentWeight;
        f /= (float)from.size();
        int originalSignalStrength = MathHelper.floor(f * 14.0F) + (numOccupiedSlots > 0 ? 1 : 0);


        ComparatorUpdatePattern updatePattern = ComparatorUpdatePattern.NO_UPDATE;
        //check the signal strength change when failing to extract from each slot
        int[] availableSlots = from instanceof SidedInventory ? ((SidedInventory) from).getAvailableSlots(Direction.DOWN) : null;
        SidedInventory sidedInventory = from instanceof SidedInventory ? (SidedInventory) from : null;
        int fromSize = availableSlots != null ? availableSlots.length : from.size();
        for (int i = 0; i < fromSize; i++) {
            int fromSlot = availableSlots != null ? availableSlots[i] : i;
            ItemStack itemStack = fromStackList.get(fromSlot);
            if (!itemStack.isEmpty() && (sidedInventory == null || sidedInventory.canExtract(fromSlot, itemStack, Direction.DOWN))) {
                int newSignalStrength = calculateReducedSignalStrength(contentWeight, from.size(), from.getMaxCountPerStack(), numOccupiedSlots, itemStack.getCount(), itemStack.getMaxCount());
                if (newSignalStrength != originalSignalStrength) {
                    updatePattern = updatePattern.thenDecrementUpdateIncrementUpdate();
                } else {
                    updatePattern = updatePattern.thenUpdate();
                }
                if (!updatePattern.isChainable()) {
                    break; //if the pattern is indistinguishable from all extensions of the pattern, stop iterating
                }
            }
        }
        return updatePattern;
    }

    public static Inventory replaceDoubleInventory(Inventory blockInventory) {
        if (blockInventory instanceof DoubleInventory doubleInventory) {
            doubleInventory = LithiumDoubleInventory.getLithiumInventory(doubleInventory);
            if (doubleInventory != null) {
                return doubleInventory;
            }
        }
        return blockInventory;
    }
}
