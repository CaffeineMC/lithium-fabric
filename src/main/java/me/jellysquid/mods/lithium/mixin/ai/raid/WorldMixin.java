package me.jellysquid.mods.lithium.mixin.ai.raid;

import me.jellysquid.mods.lithium.common.ai.raid.OminousBannerCache;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(World.class)
public abstract class WorldMixin implements OminousBannerCache {

    // The call to Raid#getOminousBanner() is very expensive, so cache it and re-use it during AI ticking
    @Unique
    private ItemStack cachedOminousBanner;

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    protected void initCachedOminousBanner(MutableWorldProperties properties, RegistryKey<?> registryRef, DynamicRegistryManager registryManager, RegistryEntry<?> dimensionEntry, Supplier<?> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates, CallbackInfo ci) {
        this.cachedOminousBanner = Raid.getOminousBanner(registryManager.getWrapperOrThrow(RegistryKeys.BANNER_PATTERN));
    }

    @Override
    public ItemStack lithium$getCachedOminousBanner() {
        return cachedOminousBanner;
    }
}
