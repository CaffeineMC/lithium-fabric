package me.jellysquid.mods.lithium.common.entity;

import me.jellysquid.mods.lithium.common.util.change_tracking.ChangeSubscriber;
import net.minecraft.item.ItemStack;

public interface EquipmentEntity {
    void lithium$onEquipmentReplaced(ItemStack oldStack, ItemStack newStack);

    interface EquipmentTrackingEntity {
        void lithium$onEquipmentChanged();
    }

    interface TickableEnchantmentTrackingEntity extends ChangeSubscriber.EnchantmentSubscriber<ItemStack> {

        void lithium$updateHasTickableEnchantments(ItemStack oldStack, ItemStack newStack);
    }
}
