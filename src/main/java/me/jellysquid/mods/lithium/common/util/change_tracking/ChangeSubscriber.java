package me.jellysquid.mods.lithium.common.util.change_tracking;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public interface ChangeSubscriber<T> {

    static <T> ChangeSubscriber<T> combine(ChangeSubscriber<T> prevSubscriber, int prevSData, @NotNull ChangeSubscriber<T> newSubscriber, int newSData) {
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
    static <T> ChangeSubscriber<T> without(ChangeSubscriber<T> prevSubscriber, ChangeSubscriber<T> removedSubscriber) {
        return without(prevSubscriber, removedSubscriber, 0, false);
    }

    static <T> ChangeSubscriber<T> without(ChangeSubscriber<T> prevSubscriber, ChangeSubscriber<T> removedSubscriber, int removedSubscriberData, boolean matchData) {
        if (prevSubscriber == removedSubscriber) {
            return null;
        } else if (prevSubscriber instanceof Multi<T> multi) {
            int index = multi.indexOf(removedSubscriber, removedSubscriberData, matchData);
            if (index != -1) {
                if (multi.subscribers.size() == 2) {
                    return multi.subscribers.get(1 - index);
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
        return dataWithout(prevSubscriber, removedSubscriber, subscriberData, 0, false);
    }

    static <T> int dataWithout(ChangeSubscriber<T> prevSubscriber, ChangeSubscriber<T> removedSubscriber, int subscriberData, int removedSubscriberData, boolean matchData) {
        if (prevSubscriber instanceof Multi<T> multi) {
            int index = multi.indexOf(removedSubscriber, removedSubscriberData, matchData);
            if (index != -1) {
                if (multi.subscribers.size() == 2) {
                    return multi.subscriberDatas.getInt(1 - index);
                } else {
                    return subscriberData;
                }
            } else {
                return subscriberData;
            }
        }
        return prevSubscriber == removedSubscriber ? 0 : subscriberData;
    }

    static int dataOf(ChangeSubscriber<?> subscribers, ChangeSubscriber<?> subscriber, int subscriberData) {
        return subscribers instanceof Multi<?> multi ? multi.subscriberDatas.getInt(multi.subscribers.indexOf(subscriber)) : subscriberData;
    }

    static boolean containsSubscriber(ChangeSubscriber<ItemStack> subscriber, int subscriberData, ChangeSubscriber<ItemStack> subscriber1, int subscriberData1) {
        if (subscriber instanceof Multi<ItemStack> multi) {
            return multi.indexOf(subscriber1, subscriberData1, true) != -1;
        }
        return subscriber == subscriber1 && subscriberData == subscriberData1;
    }


    /**
     * Notify the subscriber that the publisher will be changed immediately after this call.
     * @param publisher The publisher that is about to change
     * @param subscriberData The data associated with the subscriber, given when the subscriber was added
     */
    void lithium$notify(@Nullable T publisher, int subscriberData);

    /**
     * Notify the subscriber about being unsubscribed from the publisher. Used when the publisher becomes invalid.
     * The subscriber should not attempt to unsubscribe itself from the publisher in this method.
     *
     * @param publisher The publisher unsubscribed from
     * @param subscriberData The data associated with the subscriber, given when the subscriber was added
     */
    void lithium$forceUnsubscribe(T publisher, int subscriberData);

    interface CountChangeSubscriber<T> extends ChangeSubscriber<T> {

        /**
         * Notify the subscriber that the publisher's count data will be changed immediately after this call.
         * @param publisher The publisher that is about to change
         * @param subscriberData The data associated with the subscriber, given when the subscriber was added
         * @param newCount The new count of the publisher
         */
        void lithium$notifyCount(T publisher, int subscriberData, int newCount);
    }

    interface EnchantmentSubscriber<T> extends ChangeSubscriber<T> {

        /**
         * Notify the subscriber that the publisher's enchantment data has been changed immediately before this call.
         * @param publisher The publisher that has changed
         * @param subscriberData The data associated with the subscriber, given when the subscriber was added
         */
        void lithium$notifyAfterEnchantmentChange(T publisher, int subscriberData);
    }

    class Multi<T> implements CountChangeSubscriber<T>, EnchantmentSubscriber<T> {
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
        public void lithium$notifyCount(T publisher, int subscriberData, int newCount) {
            ArrayList<ChangeSubscriber<T>> changeSubscribers = this.subscribers;
            for (int i = 0; i < changeSubscribers.size(); i++) {
                ChangeSubscriber<T> subscriber = changeSubscribers.get(i);
                if (subscriber instanceof ChangeSubscriber.CountChangeSubscriber<T> countChangeSubscriber) {
                    countChangeSubscriber.lithium$notifyCount(publisher, this.subscriberDatas.getInt(i), newCount);
                }
            }
        }

        int indexOf(ChangeSubscriber<T> subscriber, int subscriberData, boolean matchData) {
            if (!matchData) {
                return this.subscribers.indexOf(subscriber);
            } else {
                for (int i = 0; i < this.subscribers.size(); i++) {
                    if (this.subscribers.get(i) == subscriber && this.subscriberDatas.getInt(i) == subscriberData) {
                        return i;
                    }
                }
                return -1;
            }
        }

        @Override
        public void lithium$notifyAfterEnchantmentChange(T publisher, int subscriberData) {
            ArrayList<ChangeSubscriber<T>> changeSubscribers = this.subscribers;
            for (int i = 0; i < changeSubscribers.size(); i++) {
                ChangeSubscriber<T> subscriber = changeSubscribers.get(i);
                if (subscriber instanceof ChangeSubscriber.EnchantmentSubscriber<T> enchantmentSubscriber) {
                    enchantmentSubscriber.lithium$notifyAfterEnchantmentChange(publisher, this.subscriberDatas.getInt(i));
                }
            }
        }
    }
}
