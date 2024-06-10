package me.jellysquid.mods.lithium.mixin.entity.equipment_tracking.enchantment_ticking;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import me.jellysquid.mods.lithium.common.entity.EquipmentEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements EquipmentEntity.TickableEnchantmentTrackingEntity {

    @Unique
    private boolean maybeHasTickableEnchantments = (Object) this instanceof PlayerEntity;

    @WrapWithCondition(
            method = "baseTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;onTick(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LivingEntity;)V")
    )
    private boolean maybeHasAnyTickableEnchantments(ServerWorld world, LivingEntity user) {
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
            ItemEnchantmentsComponent enchantments = stack.get(DataComponentTypes.ENCHANTMENTS);
            if (enchantments != null && !enchantments.isEmpty()) {
                for (RegistryEntry<Enchantment> enchantmentEntry : enchantments.getEnchantments()) {
                    if (!enchantmentEntry.value().getEffect(EnchantmentEffectComponentTypes.TICK).isEmpty()) {
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }
}
