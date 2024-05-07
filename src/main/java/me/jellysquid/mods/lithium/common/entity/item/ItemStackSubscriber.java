package me.jellysquid.mods.lithium.common.entity.item;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;

@Deprecated
public interface ItemStackSubscriber {

    void lithium$notifyBeforeCountChange(ItemStack itemStack, int index, int newCount);

    default void lithium$notifyAfterItemEntityStackSwap(int index, ItemEntity itemEntity, ItemStack oldStack) {}
}
