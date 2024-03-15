package me.jellysquid.mods.lithium.mixin.ai.raid;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryEntryLookup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RaiderEntity.class)
public class RaiderEntityMixin {
    // The call to Raid#getOminousBanner() is very expensive, so cache it and re-use it during AI ticking
    private static ItemStack CACHED_OMINOUS_BANNER;

    @WrapOperation(
            method = "method_16483",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/village/raid/Raid;getOminousBanner(Lnet/minecraft/registry/RegistryEntryLookup;)Lnet/minecraft/item/ItemStack;"
            )
    )
    private static ItemStack getOminousBanner(RegistryEntryLookup<BannerPattern> bannerPatternLookup, Operation<ItemStack> original) {
        if (CACHED_OMINOUS_BANNER == null) {
            CACHED_OMINOUS_BANNER = original.call(bannerPatternLookup);
        }
        return CACHED_OMINOUS_BANNER;
    }

    @WrapOperation(
            method = {"loot", "onDeath"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/village/raid/Raid;getOminousBanner(Lnet/minecraft/registry/RegistryEntryLookup;)Lnet/minecraft/item/ItemStack;"
            )
    )
    private ItemStack getOminousBanner2(RegistryEntryLookup<BannerPattern> bannerPatternLookup, Operation<ItemStack> original) {
        if (CACHED_OMINOUS_BANNER == null) {
            CACHED_OMINOUS_BANNER = original.call(bannerPatternLookup);
        }
        return CACHED_OMINOUS_BANNER;
    }
}
