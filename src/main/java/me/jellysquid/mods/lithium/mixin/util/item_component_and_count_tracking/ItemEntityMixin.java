package me.jellysquid.mods.lithium.mixin.util.item_component_and_count_tracking;

import me.jellysquid.mods.lithium.common.util.change_tracking.ChangePublisher;
import me.jellysquid.mods.lithium.common.util.change_tracking.ChangeSubscriber;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin implements ChangePublisher<ItemEntity>, ChangeSubscriber.CountChangeSubscriber<ItemStack> {

    @Shadow
    public abstract ItemStack getStack();

    @Unique
    private ChangeSubscriber<ItemEntity> subscriber;
    @Unique
    //Stores the data of the subscriber, unless the subscriber is a Multi which stores the data in a list, in which case this variable stores 0
    private int subscriberData;

    @Unique
    private void startTrackingChanges() {
        ItemStack stack = this.getStack();
        if (!stack.isEmpty()) {
            //noinspection unchecked
            ((ChangePublisher<ItemStack>) (Object) stack).lithium$subscribe(this, 0);
        }
    }

    @Override
    public void lithium$subscribe(ChangeSubscriber<ItemEntity> subscriber, int subscriberData) {
        if (this.subscriber == null) {
            this.startTrackingChanges();
        }
        this.subscriber = ChangeSubscriber.combine(this.subscriber, this.subscriberData, subscriber, subscriberData);
        if (this.subscriber instanceof ChangeSubscriber.Multi<?>) {
            this.subscriberData = 0;
        } else {
            this.subscriberData = subscriberData;
        }
    }

    @Override
    public int lithium$unsubscribe(ChangeSubscriber<ItemEntity> subscriber) {
        int retval = ChangeSubscriber.dataOf(this.subscriber, subscriber, this.subscriberData);
        this.subscriberData = ChangeSubscriber.dataWithout(this.subscriber, subscriber, this.subscriberData);
        this.subscriber = ChangeSubscriber.without(this.subscriber, subscriber);

        if (this.subscriber == null) {
            ItemStack stack = this.getStack();
            if (!stack.isEmpty()) {
                //noinspection unchecked
                ((ChangePublisher<ItemStack>) (Object) stack).lithium$unsubscribe(this);
            }
        }
        return retval;
    }

    @Override
    public void lithium$notify(ItemStack publisher, int subscriberData) {
        if (publisher != this.getStack()) {
            throw new IllegalStateException("Received notification from an unexpected publisher");
        }

        if (this.subscriber != null) {
            this.subscriber.lithium$notify((ItemEntity) (Object) this, this.subscriberData);
        }
    }

    @Override
    public void lithium$forceUnsubscribe(ItemStack publisher, int subscriberData) {
        if (this.subscriber != null) {
            this.subscriber.lithium$forceUnsubscribe((ItemEntity) (Object) this, this.subscriberData);
            this.subscriber = null;
            this.subscriberData = 0;
        }
    }

    @Override
    public void lithium$notifyCount(ItemStack publisher, int subscriberData, int newCount) {
        if (publisher != this.getStack()) {
            throw new IllegalStateException("Received notification from an unexpected publisher");
        }

        if (this.subscriber instanceof ChangeSubscriber.CountChangeSubscriber<ItemEntity> countChangeSubscriber) {
            countChangeSubscriber.lithium$notifyCount((ItemEntity) (Object) this, this.subscriberData, newCount);
        }
    }

    @Inject(method = "setStack", at = @At("HEAD"))
    private void beforeSetStack(ItemStack newStack, CallbackInfo ci) {
        if (this.subscriber != null) {
            ItemStack oldStack = this.getStack();
            if (oldStack != newStack) {
                if (!oldStack.isEmpty()) {
                    //noinspection unchecked
                    ((ChangePublisher<ItemStack>) (Object) oldStack).lithium$unsubscribe(this);
                }

                if (!newStack.isEmpty()) {
                    //noinspection unchecked
                    ((ChangePublisher<ItemStack>) (Object) newStack).lithium$subscribe(this, this.subscriberData);
                    this.subscriber.lithium$notify((ItemEntity) (Object) this, this.subscriberData);
                } else {
                    this.subscriber.lithium$forceUnsubscribe((ItemEntity) (Object) this, this.subscriberData);
                    this.subscriber = null;
                    this.subscriberData = 0;
                }
            }
        }
    }
}
