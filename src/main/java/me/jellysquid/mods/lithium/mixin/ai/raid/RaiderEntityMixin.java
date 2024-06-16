package me.jellysquid.mods.lithium.mixin.ai.raid;

import me.jellysquid.mods.lithium.common.world.LithiumData;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

@Mixin(RaiderEntity.class)
public abstract class RaiderEntityMixin extends Entity {
    @Mutable
    @Shadow
    @Final
    static Predicate<ItemEntity> OBTAINABLE_OMINOUS_BANNER_PREDICATE;

    static {
        // The call to Raid#getOminousBanner() is very expensive, use a cached banner during AI ticking
        OBTAINABLE_OMINOUS_BANNER_PREDICATE = (itemEntity) -> {
            ItemStack ominousBanner = ((LithiumData) itemEntity.getWorld()).lithium$getData().ominousBanner();
            if (ominousBanner == null) {
                ominousBanner = Raid.getOminousBanner(itemEntity.getRegistryManager().getWrapperOrThrow(RegistryKeys.BANNER_PATTERN));
            }

            return !itemEntity.cannotPickup() && itemEntity.isAlive() &&
                    ItemStack.areEqual(itemEntity.getStack(), ominousBanner);
        };
    }

    public RaiderEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Redirect(
            method = {"loot(Lnet/minecraft/entity/ItemEntity;)V", "isCaptain()Z"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/village/raid/Raid;getOminousBanner(Lnet/minecraft/registry/RegistryEntryLookup;)Lnet/minecraft/item/ItemStack;")
    )
    private ItemStack getOminousBanner(RegistryEntryLookup<BannerPattern> bannerPatternLookup) {
        ItemStack ominousBanner = ((LithiumData) this.getWorld()).lithium$getData().ominousBanner();
        if (ominousBanner == null) {
            ominousBanner = Raid.getOminousBanner(bannerPatternLookup);
        }
        return ominousBanner;
    }
}
