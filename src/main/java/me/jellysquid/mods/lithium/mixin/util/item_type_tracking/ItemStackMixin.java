package me.jellysquid.mods.lithium.mixin.util.item_type_tracking;

import me.jellysquid.mods.lithium.common.util.change_tracking.ChangePublisher;
import me.jellysquid.mods.lithium.common.util.change_tracking.ChangeSubscriber;
import net.minecraft.component.ComponentMapImpl;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ChangePublisher<ItemStack>, ChangeSubscriber<ComponentMapImpl> {

    @Shadow
    @Final
    ComponentMapImpl components;

    @Shadow
    private int count;

    @Unique
    private ChangeSubscriber<ItemStack> subscriber;
    @Unique
    private int subscriberData;

    @Override
    public boolean lithium$subscribe(ChangeSubscriber<ItemStack> subscriber, int subscriberData) {
        if (this.subscriber == null) {
            boolean b = this.startTrackingChanges();
            if (!b) {
                return false;
            }
        }
        this.subscriber = ChangeSubscriber.combine(this.subscriber, this.subscriberData, subscriber, subscriberData);
        if (this.subscriber instanceof ChangeSubscriber.Multi<?>) {
            this.subscriberData = 0;
        } else {
            this.subscriberData = subscriberData;
        }
        return false;
    }

    @Override
    public void lithium$unsubscribe(ChangeSubscriber<ItemStack> subscriber) {
        this.subscriberData = ChangeSubscriber.dataWithout(this.subscriber, subscriber, this.subscriberData);
        this.subscriber = ChangeSubscriber.without(this.subscriber, subscriber);

        if (this.subscriber == null) {
            //noinspection unchecked
            ((ChangePublisher<ComponentMapImpl>) (Object) this.components).lithium$unsubscribe(this);
        }
    }

    @Unique
    private boolean startTrackingChanges() {
        //Safe because ComponentMapImplMixin
        //noinspection unchecked
        boolean b = ((ChangePublisher<ComponentMapImpl>) (Object) this.components).lithium$subscribe(this, 0);
        if (!b) {
            throw new IllegalStateException("Failed to subscribe to component map!");
        }
        return b;
    }

    @Inject(method = "setCount(I)V", at = @At("HEAD"))
    private void unsubscribeOnEmpty(int count, CallbackInfo ci) {
        if (count != this.count) {

            if (this.subscriber instanceof ChangeSubscriber.CountChangeSubscriber<ItemStack> countChangeSubscriber) {
                countChangeSubscriber.lithium$notifyCount((ItemStack) (Object) this, this.subscriberData, count);
            }

            if (count == 0) {
                //Safe because ComponentMapImplMixin
                //noinspection unchecked
                ((ChangePublisher<ComponentMapImpl>) (Object) this.components).lithium$unsubscribe(this);

                if (this.subscriber != null) {
                    this.subscriber.lithium$forceUnsubscribe((ItemStack) (Object) this, this.subscriberData);
                }
            }
        }

    }

    @Override
    public void lithium$notify(ComponentMapImpl publisher, int subscriberData) {
        if (this.subscriber != null) {
            this.subscriber.lithium$notify((ItemStack) (Object) this, this.subscriberData);
        }
    }
}
