package net.caffeinemc.mods.lithium.mixin.ai.raid;

import net.caffeinemc.mods.lithium.common.world.LithiumData;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

@Mixin(Raider.class)
public abstract class RaiderEntityMixin extends Entity {
    @Mutable
    @Shadow
    @Final
    static Predicate<ItemEntity> ALLOWED_ITEMS;

    static {
        // The call to Raid#getOminousBanner() is very expensive, use a cached banner during AI ticking
        ALLOWED_ITEMS = (itemEntity) -> {
            ItemStack ominousBanner = ((LithiumData) itemEntity.level()).lithium$getData().ominousBanner();
            if (ominousBanner == null) {
                ominousBanner = Raid.getLeaderBannerInstance(itemEntity.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN));
            }

            return !itemEntity.hasPickUpDelay() && itemEntity.isAlive() &&
                    ItemStack.matches(itemEntity.getItem(), ominousBanner);
        };
    }

    public RaiderEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Redirect(
            method = {"pickUpItem(Lnet/minecraft/world/entity/item/ItemEntity;)V", "isCaptain()Z"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/raid/Raid;getLeaderBannerInstance(Lnet/minecraft/core/HolderGetter;)Lnet/minecraft/world/item/ItemStack;")
    )
    private ItemStack getOminousBanner(HolderGetter<BannerPattern> bannerPatternLookup) {
        ItemStack ominousBanner = ((LithiumData) this.level()).lithium$getData().ominousBanner();
        if (ominousBanner == null) {
            ominousBanner = Raid.getLeaderBannerInstance(bannerPatternLookup);
        }
        return ominousBanner;
    }
}
