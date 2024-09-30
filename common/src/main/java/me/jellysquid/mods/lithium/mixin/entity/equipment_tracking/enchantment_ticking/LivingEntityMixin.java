package me.jellysquid.mods.lithium.mixin.entity.equipment_tracking.enchantment_ticking;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import me.jellysquid.mods.lithium.common.entity.EquipmentEntity;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements EquipmentEntity.TickableEnchantmentTrackingEntity {

    @Unique
    private boolean maybeHasTickableEnchantments = (Object) this instanceof Player;

    @WrapWithCondition(
            method = "baseTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;tickEffects(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)V")
    )
    private boolean maybeHasAnyTickableEnchantments(ServerLevel world, LivingEntity user) {
        return this.maybeHasTickableEnchantments;
    }


    @Override
    public void lithium$updateHasTickableEnchantments(ItemStack oldStack, ItemStack newStack) {
        if (!this.maybeHasTickableEnchantments) {
            this.maybeHasTickableEnchantments = stackHasTickableEnchantment(newStack);
        }
    }


    @Override
    public void lithium$notifyAfterEnchantmentChange(ItemStack publisher, int subscriberData) {
        if (!this.maybeHasTickableEnchantments) {
            this.maybeHasTickableEnchantments = stackHasTickableEnchantment(publisher);
        }
    }

    @Unique
    private static boolean stackHasTickableEnchantment(ItemStack stack) {
        if (!stack.isEmpty()) {
            ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
            if (enchantments != null && !enchantments.isEmpty()) {
                for (Holder<Enchantment> enchantmentEntry : enchantments.keySet()) {
                    if (!enchantmentEntry.value().getEffects(EnchantmentEffectComponents.TICK).isEmpty()) {
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }
}
