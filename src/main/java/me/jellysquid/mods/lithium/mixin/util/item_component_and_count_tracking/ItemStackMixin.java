package me.jellysquid.mods.lithium.mixin.util.item_component_and_count_tracking;

import me.jellysquid.mods.lithium.common.util.change_tracking.ChangePublisher;
import me.jellysquid.mods.lithium.common.util.change_tracking.ChangeSubscriber;
import net.minecraft.component.ComponentMapImpl;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ChangePublisher<ItemStack>, ChangeSubscriber<ComponentMapImpl> {

    @Shadow
    @Final
    ComponentMapImpl components;

    @Shadow
    private int count;

    @Shadow
    public abstract boolean isEmpty();

    @Unique
    private ChangeSubscriber<ItemStack> subscriber;
    @Unique
    private int subscriberData;

    @Override
    public void lithium$subscribe(ChangeSubscriber<ItemStack> subscriber, int subscriberData) {
        if (this.isEmpty()) {
            throw new IllegalStateException("Cannot subscribe to an empty ItemStack!");
        }

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
    public int lithium$unsubscribe(ChangeSubscriber<ItemStack> subscriber) {
        if (this.isEmpty()) {
            throw new IllegalStateException("Cannot unsubscribe from an empty ItemStack!");
        }

        int retval = ChangeSubscriber.dataOf(this.subscriber, subscriber, this.subscriberData);
        this.subscriberData = ChangeSubscriber.dataWithout(this.subscriber, subscriber, this.subscriberData);
        this.subscriber = ChangeSubscriber.without(this.subscriber, subscriber);

        if (this.subscriber == null) {
            //noinspection unchecked
            ((ChangePublisher<ComponentMapImpl>) (Object) this.components).lithium$unsubscribe(this);
        }
        return retval;
    }

    @Override
    public void lithium$unsubscribeWithData(ChangeSubscriber<ItemStack> subscriber, int subscriberData) {
        if (this.isEmpty()) {
            throw new IllegalStateException("Cannot unsubscribe from an empty ItemStack!");
        }

        this.subscriberData = ChangeSubscriber.dataWithout(this.subscriber, subscriber, this.subscriberData, subscriberData, true);
        this.subscriber = ChangeSubscriber.without(this.subscriber, subscriber, subscriberData, true);

        if (this.subscriber == null) {
            //noinspection unchecked
            ((ChangePublisher<ComponentMapImpl>) (Object) this.components).lithium$unsubscribe(this);
        }
    }

    @Override
    public boolean lithium$isSubscribedWithData(ChangeSubscriber<ItemStack> subscriber, int subscriberData) {
        if (this.isEmpty()) {
            throw new IllegalStateException("Cannot be subscribed to an empty ItemStack!");
        }

        return ChangeSubscriber.containsSubscriber(this.subscriber, this.subscriberData, subscriber, subscriberData);
    }

    @Override
    public void lithium$forceUnsubscribe(ComponentMapImpl publisher, int subscriberData) {
        if (publisher != this.components) {
            throw new IllegalStateException("Invalid publisher, expected " + this.components + " but got " + publisher);
        }
        this.subscriber.lithium$forceUnsubscribe((ItemStack) (Object) this, this.subscriberData);
        this.subscriber = null;
        this.subscriberData = 0;
    }

    @Unique
    private void startTrackingChanges() {
        //Safe because ComponentMapImplMixin
        //noinspection unchecked
        ((ChangePublisher<ComponentMapImpl>) (Object) this.components).lithium$subscribe(this, 0);
    }

    @Inject(method = "setCount(I)V", at = @At("HEAD"))
    private void beforeChangeCount(int count, CallbackInfo ci) {
        if (count != this.count) {

            if (this.subscriber instanceof ChangeSubscriber.CountChangeSubscriber<ItemStack> countChangeSubscriber) {
                countChangeSubscriber.lithium$notifyCount((ItemStack) (Object) this, this.subscriberData, count);
            }

            if (count == 0) {
                //Safe because ComponentMapImplMixin implements the interface
                //noinspection unchecked
                ((ChangePublisher<ComponentMapImpl>) (Object) this.components).lithium$unsubscribe(this);

                if (this.subscriber != null) {
                    this.subscriber.lithium$forceUnsubscribe((ItemStack) (Object) this, this.subscriberData);
                    this.subscriber = null;
                    this.subscriberData = 0;
                }
            }
        }

    }

    @Override
    public void lithium$notify(ComponentMapImpl publisher, int subscriberData) {
        if (publisher != this.components) {
            throw new IllegalStateException("Invalid publisher, expected " + this.components + " but got " + publisher);
        }

        if (this.subscriber != null) {
            this.subscriber.lithium$notify((ItemStack) (Object) this, this.subscriberData);
        }
    }

    @Inject(
            method = "set(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;",
            at = @At("RETURN")
    )
    private <T> void onSetComponent(ComponentType<? super T> type, @Nullable T value, CallbackInfoReturnable<T> cir) {
        if (type == DataComponentTypes.ENCHANTMENTS) {
            if (this.subscriber instanceof ChangeSubscriber.EnchantmentSubscriber<ItemStack> enchantmentSubscriber) {
                enchantmentSubscriber.lithium$notifyAfterEnchantmentChange((ItemStack) (Object) this, this.subscriberData);
            }
        }
    }
}
