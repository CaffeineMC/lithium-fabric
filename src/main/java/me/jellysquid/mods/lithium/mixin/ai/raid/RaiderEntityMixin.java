package me.jellysquid.mods.lithium.mixin.ai.raid;

import me.jellysquid.mods.lithium.common.ai.raid.OminousBannerCache;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryEntryLookup;
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
        OBTAINABLE_OMINOUS_BANNER_PREDICATE = (itemEntity) -> !itemEntity.cannotPickup() && itemEntity.isAlive() &&
                ItemStack.areEqual(itemEntity.getStack(), ((OminousBannerCache) itemEntity.getWorld()).lithium$getCachedOminousBanner());
    }

    public RaiderEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Redirect(
            method = "onDeath(Lnet/minecraft/entity/damage/DamageSource;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/village/raid/Raid;getOminousBanner(Lnet/minecraft/registry/RegistryEntryLookup;)Lnet/minecraft/item/ItemStack;")
    )
    private ItemStack getOminousBanner(RegistryEntryLookup<BannerPattern> bannerPatternLookup) {
        return ((OminousBannerCache) this.getWorld()).lithium$getCachedOminousBanner();
    }
}
