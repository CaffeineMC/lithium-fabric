package me.jellysquid.mods.lithium.common.entity.item;

public interface ItemStackSubscriber {

    void lithium$notifyBeforeCountChange(int slot, int newCount);
}
