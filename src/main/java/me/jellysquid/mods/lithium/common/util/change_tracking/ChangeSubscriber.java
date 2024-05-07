package me.jellysquid.mods.lithium.common.util.change_tracking;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public interface ChangeSubscriber<T> {

    static <T> ChangeSubscriber<T> add(ChangeSubscriber<T> prevSubscriber, int prevSData, @NotNull ChangeSubscriber<T> newSubscriber, int newSData) {
        if (prevSubscriber == null) {
            return newSubscriber;
        } else if (prevSubscriber instanceof Multi) {
            ArrayList<ChangeSubscriber<T>> subscribers = new ArrayList<>(((Multi<T>) prevSubscriber).subscribers);
            IntArrayList subscriberDatas = new IntArrayList(((Multi<T>) prevSubscriber).subscriberDatas);
            subscribers.add(newSubscriber);
            subscriberDatas.add(newSData);
            return new Multi<>(subscribers, subscriberDatas);
        } else {
            ArrayList<ChangeSubscriber<T>> subscribers = new ArrayList<>();
            IntArrayList subscriberDatas = new IntArrayList();
            subscribers.add(prevSubscriber);
            subscriberDatas.add(prevSData);
            subscribers.add(newSubscriber);
            subscriberDatas.add(newSData);
            return new Multi<>(subscribers, subscriberDatas);
        }
    }

    static <T> ChangeSubscriber<T> remove(ChangeSubscriber<T> prevSubscriber, ChangeSubscriber<T> removedSubscriber) {
        if (prevSubscriber == removedSubscriber) {
            return null;
        } else if (prevSubscriber instanceof Multi<T> multi) {
            int index = multi.subscribers.indexOf(removedSubscriber);
            if (index != -1) {
                if (multi.subscribers.size() == 2) {
                    return multi.subscribers.get(0) == removedSubscriber ? multi.subscribers.get(1) : multi.subscribers.get(0);
                } else {
                    ArrayList<ChangeSubscriber<T>> subscribers = new ArrayList<>(multi.subscribers);
                    IntArrayList subscriberDatas = new IntArrayList(multi.subscriberDatas);
                    subscribers.remove(index);
                    subscriberDatas.removeInt(index);

                    return new Multi<>(subscribers, subscriberDatas);
                }
            } else {
                return prevSubscriber;
            }
        } else {
            return prevSubscriber;
        }
    }

    static <T> int dataWithout(ChangeSubscriber<T> prevSubscriber, ChangeSubscriber<T> removedSubscriber, int subscriberData) {
        if (prevSubscriber instanceof Multi<T> multi) {
            if (multi.subscribers.size() == 2) {
                int i = multi.subscribers.indexOf(removedSubscriber);
                if (i == -1) {
                    return subscriberData;
                } else {
                    return multi.subscriberDatas.getInt(1 - i);
                }
            }
            if (multi.subscribers.size() == 1) {
                prevSubscriber = multi.subscribers.get(0);
            } else {
                return 0;
            }
        }
        return prevSubscriber == removedSubscriber ? 0 : subscriberData;
    }


    void lithium$notify(@Nullable T publisher, int subscriberData);

    void lithium$forceUnsubscribe(T publisher, int subscriberData);

    interface ItemCountChangeSubscriber<T> extends ChangeSubscriber<T> {
        void lithium$notifyBeforeCountChange(T publisher, int sData, int newCount);
    }

    class Multi<T> implements ItemCountChangeSubscriber<T> {
        private final ArrayList<ChangeSubscriber<T>> subscribers;
        private final IntArrayList subscriberDatas;

        public Multi(ArrayList<ChangeSubscriber<T>> subscribers, IntArrayList subscriberDatas) {
            this.subscribers = subscribers;
            this.subscriberDatas = subscriberDatas;
        }

        @Override
        public void lithium$notify(T publisher, int subscriberData) {
            ArrayList<ChangeSubscriber<T>> changeSubscribers = this.subscribers;
            for (int i = 0; i < changeSubscribers.size(); i++) {
                ChangeSubscriber<T> subscriber = changeSubscribers.get(i);
                subscriber.lithium$notify(publisher, this.subscriberDatas.getInt(i));
            }
        }

        @Override
        public void lithium$forceUnsubscribe(T publisher, int subscriberData) {
            ArrayList<ChangeSubscriber<T>> changeSubscribers = this.subscribers;
            for (int i = 0; i < changeSubscribers.size(); i++) {
                ChangeSubscriber<T> subscriber = changeSubscribers.get(i);
                subscriber.lithium$forceUnsubscribe(publisher, this.subscriberDatas.getInt(i));
            }
        }

        @Override
        public void lithium$notifyBeforeCountChange(T publisher, int sData, int newCount) {
            ArrayList<ChangeSubscriber<T>> changeSubscribers = this.subscribers;
            for (int i = 0; i < changeSubscribers.size(); i++) {
                ChangeSubscriber<T> subscriber = changeSubscribers.get(i);
                if (subscriber instanceof ItemCountChangeSubscriber<T> itemCountChangeSubscriber) {
                    itemCountChangeSubscriber.lithium$notifyBeforeCountChange(publisher, this.subscriberDatas.getInt(i), newCount);
                }
            }
        }
    }
}
