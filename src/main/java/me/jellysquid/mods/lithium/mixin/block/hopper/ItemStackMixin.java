package me.jellysquid.mods.lithium.mixin.block.hopper;

import me.jellysquid.mods.lithium.common.hopper.LithiumStackList;
import me.jellysquid.mods.lithium.common.hopper.StorableItemStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements StorableItemStack {

    private static final LithiumStackList DUMMY_STACK_LIST = new LithiumStackList(DefaultedList.of(), 0);
    @Shadow
    private int count;

    private int mySlot;

    @Nullable
    private LithiumStackList myLocation;

    @Override
    public void registerToInventory(LithiumStackList itemStacks, int mySlot) {
        if (this.myLocation != null) {
            this.myLocation.setUnstableInteractionConditions();
            itemStacks.setUnstableInteractionConditions();
            this.myLocation = DUMMY_STACK_LIST;
        } else {
            this.myLocation = itemStacks;
            this.mySlot = mySlot;
        }
    }

    @Override
    public void unregisterFromInventory(LithiumStackList myInventoryList) {
        if (myInventoryList == DUMMY_STACK_LIST) {
            return;
        }
        assert this.myLocation == myInventoryList;
        this.myLocation = null;
    }

    @ModifyVariable(method = "setCount(I)V", at = @At("HEAD"))
    public int updateInventory(int count) {
        if (this.myLocation != null && this.myLocation != DUMMY_STACK_LIST && this.count != count) {
            this.myLocation.beforeSlotCountChange(this.mySlot, count);
        }
        return count;
    }
}
