package me.jellysquid.mods.lithium.common.util.change_tracking;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public interface ChangeSubscriber<T> {

    static <T> ChangeSubscriber<T> add(ChangeSubscriber<T> prevSubscriber, @NotNull ChangeSubscriber<T> newSubscriber) {
        if (prevSubscriber == null) {
            return newSubscriber;
        } else if (prevSubscriber instanceof Multi) {
            ((Multi<T>) prevSubscriber).subscribers.add(newSubscriber);
            return prevSubscriber;
        } else {
            ArrayList<ChangeSubscriber<T>> subscribers = new ArrayList<>();
            subscribers.add(prevSubscriber);
            subscribers.add(newSubscriber);
            return new Multi<>(subscribers);
        }
    }

    static <T> ChangeSubscriber<T> remove(ChangeSubscriber<T> prevSubscriber, ChangeSubscriber<T> removedSubscriber) {
        if (prevSubscriber == removedSubscriber) {
            return null;
        } else if (prevSubscriber instanceof Multi) {
            ArrayList<ChangeSubscriber<T>> subscribers = ((Multi<T>) prevSubscriber).subscribers;
            subscribers.remove(removedSubscriber);
            if (subscribers.size() == 1) {
                return subscribers.get(0);
            } else {
                return prevSubscriber;
            }
        } else {
            return prevSubscriber;
        }
    }

    void lithium$notify(@Nullable T publisher);

    void lithium$forceUnsubscribe(T publisher);

    class Multi<T> implements ChangeSubscriber<T> {
        private final ArrayList<ChangeSubscriber<T>> subscribers;

        public Multi(ArrayList<ChangeSubscriber<T>> subscribers) {
            this.subscribers = subscribers;
        }

        @Override
        public void lithium$notify(T publisher) {
            for (ChangeSubscriber<T> subscriber : this.subscribers) {
                subscriber.lithium$notify(publisher);
            }
        }

        @Override
        public void lithium$forceUnsubscribe(T publisher) {
            for (ChangeSubscriber<T> subscriber : this.subscribers) {
                subscriber.lithium$forceUnsubscribe(publisher);
            }
        }
    }
}
