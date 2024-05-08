package me.jellysquid.mods.lithium.common.util.change_tracking;

public interface ChangePublisher<T> {
    void lithium$subscribe(ChangeSubscriber<T> subscriber, int subscriberData);

    int lithium$unsubscribe(ChangeSubscriber<T> subscriber);
}
