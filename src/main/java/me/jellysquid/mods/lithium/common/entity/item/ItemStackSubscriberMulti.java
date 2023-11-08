package me.jellysquid.mods.lithium.common.entity.item;

import java.util.Arrays;

/**
 * Handling shadow items in multiple inventories or item entities
 */
public class ItemStackSubscriberMulti implements ItemStackSubscriber {
    private final ItemStackSubscriber[] subscribers;
    private final int[] slots;

    public ItemStackSubscriberMulti(ItemStackSubscriber subscriber1, int slot1, ItemStackSubscriber subscriber2, int slot2) {
        if (subscriber1 == subscriber2 && slot1 == slot2) {
            throw new IllegalArgumentException("Cannot create a multi-subscriber with two identical subscribers");
        }
        this.subscribers = new ItemStackSubscriber[]{subscriber1, subscriber2};
        this.slots = new int[]{slot1, slot2};
    }
    private ItemStackSubscriberMulti(ItemStackSubscriber[] subscribers, int[] slots) {
        if (subscribers.length <= 1) {
            throw new IllegalArgumentException("Cannot create a multi-subscriber with only one subscriber");
        }
        if (subscribers.length != slots.length) {
            throw new IllegalArgumentException("Cannot create a multi-subscriber with different subscriber and slot lengths");
        }

        if (Arrays.asList(subscribers).contains(null)) {
            throw new IllegalArgumentException("Cannot create a multi-subscriber with null subscribers");
        }

        this.subscribers = subscribers;
        this.slots = slots;
    }

    public ItemStackSubscriberMulti with(ItemStackSubscriber subscriber, int slot) {
        ItemStackSubscriber[] itemStackSubscribers = this.subscribers;
        for (int i = 0; i < itemStackSubscribers.length; i++) {
            ItemStackSubscriber sub = itemStackSubscribers[i];
            if (sub == subscriber && this.slots[i] == slot) {
                return this;
            }
        }

        ItemStackSubscriber[] newSubscribers = new ItemStackSubscriber[this.subscribers.length + 1];
        int[] newSlots = new int[this.slots.length + 1];
        System.arraycopy(this.subscribers, 0, newSubscribers, 0, this.subscribers.length);
        System.arraycopy(this.slots, 0, newSlots, 0, this.slots.length);
        newSubscribers[this.subscribers.length] = subscriber;
        newSlots[this.slots.length] = slot;
        return new ItemStackSubscriberMulti(newSubscribers, newSlots);
    }

    public ItemStackSubscriber without(ItemStackSubscriber subscriber, int index) {
        ItemStackSubscriber[] newSubscribers = new ItemStackSubscriber[this.subscribers.length - 1];
        int[] newSlots = new int[this.slots.length - 1];
        int i = 0;

        for (int j = 0; j < this.subscribers.length; j++) {
            if (this.subscribers[j] != subscriber || (index == -1 && this.slots[j] != index)) {
                if (i == newSubscribers.length) {
                    return this; // not in this multi-subscriber, no change
                }
                newSubscribers[i] = this.subscribers[j];
                newSlots[i] = this.slots[j];
                i++;
            }
        }

        if (i < newSubscribers.length) {
            newSubscribers = Arrays.copyOf(newSubscribers, i);
            newSlots = Arrays.copyOf(newSlots, i);
        }

        return newSubscribers.length == 1 ? newSubscribers[0] : new ItemStackSubscriberMulti(newSubscribers, newSlots);
    }

    public int getSlot(ItemStackSubscriber subscriber) {
        for (int i = 0; i < this.subscribers.length; i++) {
            if (this.subscribers[i] == subscriber) {
                return this.slots[i];
            }
        }
        return -1;
    }

    @Override
    public void lithium$notifyBeforeCountChange(int slot, int newCount) {
        ItemStackSubscriber[] itemStackSubscribers = this.subscribers;
        for (int i = 0; i < itemStackSubscribers.length; i++) {
            ItemStackSubscriber subscriber = itemStackSubscribers[i];
            subscriber.lithium$notifyBeforeCountChange(this.slots[i], newCount);
        }
    }
}
