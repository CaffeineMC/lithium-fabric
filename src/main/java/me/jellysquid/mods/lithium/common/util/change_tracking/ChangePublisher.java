package me.jellysquid.mods.lithium.common.util.change_tracking;

import net.minecraft.item.ItemStack;

public interface ChangePublisher<T> {
    void lithium$subscribe(ChangeSubscriber<T> subscriber, int subscriberData);

    int lithium$unsubscribe(ChangeSubscriber<T> subscriber);

    default void lithium$unsubscribeWithData(ChangeSubscriber<T> subscriber, int index) {
        throw new UnsupportedOperationException("Only implemented for ItemStacks");
    }

    default boolean lithium$isSubscribedWithData(ChangeSubscriber<ItemStack> subscriber, int subscriberData) {
        throw new UnsupportedOperationException("Only implemented for ItemStacks");
    }
}
