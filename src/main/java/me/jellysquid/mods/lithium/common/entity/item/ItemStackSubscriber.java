package me.jellysquid.mods.lithium.common.entity.item;

public interface ItemStackSubscriber {

    void notifyBeforeCountChange(int slot, int newCount);
}
