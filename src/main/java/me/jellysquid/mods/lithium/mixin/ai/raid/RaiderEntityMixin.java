package me.jellysquid.mods.lithium.mixin.ai.raid;

import net.minecraft.block.entity.BannerPattern;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.village.raid.Raid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

@Mixin(RaiderEntity.class)
public class RaiderEntityMixin {
    // The call to Raid#getOminousBanner() is very expensive, so cache it and re-use it during AI ticking
    private static final ItemStack CACHED_OMINOUS_BANNER = Raid.getOminousBanner(BuiltinRegistries.createWrapperLookup().getWrapperOrThrow(RegistryKeys.BANNER_PATTERN));
    @Mutable
    @Shadow
    @Final
    static Predicate<ItemEntity> OBTAINABLE_OMINOUS_BANNER_PREDICATE;

    static {
        OBTAINABLE_OMINOUS_BANNER_PREDICATE = (itemEntity) -> !itemEntity.cannotPickup() && itemEntity.isAlive() && ItemStack.areEqual(itemEntity.getStack(), CACHED_OMINOUS_BANNER);
    }

    @Redirect(
            method = "onDeath(Lnet/minecraft/entity/damage/DamageSource;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/village/raid/Raid;getOminousBanner(Lnet/minecraft/registry/RegistryEntryLookup;)Lnet/minecraft/item/ItemStack;")
    )
    private ItemStack getOminousBanner(RegistryEntryLookup<BannerPattern> registryEntryLookup) {
        return CACHED_OMINOUS_BANNER;
    }
}
