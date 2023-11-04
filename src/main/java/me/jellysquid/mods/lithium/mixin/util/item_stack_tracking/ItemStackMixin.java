package me.jellysquid.mods.lithium.mixin.util.item_stack_tracking;

import me.jellysquid.mods.lithium.common.entity.item.ItemStackSubscriber;
import me.jellysquid.mods.lithium.common.entity.item.ItemStackSubscriberMulti;
import me.jellysquid.mods.lithium.common.hopper.NotifyingItemStack;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements NotifyingItemStack {

    @Shadow
    private int count;

    @Unique
    private int mySlot;

    @Unique
    @Nullable
    private ItemStackSubscriber stackChangeSubscriber;


    @ModifyVariable(method = "setCount(I)V", at = @At("HEAD"), argsOnly = true)
    public int updateInventory(int count) {
        if (this.stackChangeSubscriber != null && this.count != count) {
            this.stackChangeSubscriber.notifyBeforeCountChange(this.mySlot, count);
        }
        return count;
    }

    @Override
    public void lithium$subscribe(ItemStackSubscriber subscriber) {
        this.lithium$subscribeWithIndex(subscriber, -1);
    }

    @Override
    public void lithium$subscribeWithIndex(ItemStackSubscriber subscriber, int mySlot) {
        if (this.stackChangeSubscriber != null) {
            this.lithium$registerMultipleSubscribers(subscriber, mySlot);
        } else {
            this.stackChangeSubscriber = subscriber;
            this.mySlot = mySlot;
        }
    }

    @Override
    public void lithium$unsubscribe(ItemStackSubscriber stackList) {
        this.lithium$unsubscribeWithIndex(stackList, -1);
    }

    @Override
    public void lithium$unsubscribeWithIndex(ItemStackSubscriber myInventoryList, int index) {
        if (this.stackChangeSubscriber == myInventoryList) {
            this.stackChangeSubscriber = null;
            this.mySlot = -1;
        } else if (this.stackChangeSubscriber instanceof ItemStackSubscriberMulti multiSubscriber) {
            this.stackChangeSubscriber = multiSubscriber.without(myInventoryList, index);
            this.mySlot = multiSubscriber.getSlot(this.stackChangeSubscriber);
        }  //else: no change, since the inventory wasn't subscribed
    }

    private void lithium$registerMultipleSubscribers(ItemStackSubscriber subscriber, int slot) {
        if (this.stackChangeSubscriber instanceof ItemStackSubscriberMulti multiSubscriber) {
            this.stackChangeSubscriber = multiSubscriber.with(subscriber, slot);
        } else {
            this.stackChangeSubscriber = new ItemStackSubscriberMulti(this.stackChangeSubscriber, this.mySlot, subscriber, slot);
            this.mySlot = -1;
        }
    }
}
