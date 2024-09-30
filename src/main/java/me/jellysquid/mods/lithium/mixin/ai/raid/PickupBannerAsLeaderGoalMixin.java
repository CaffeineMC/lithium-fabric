package me.jellysquid.mods.lithium.mixin.ai.raid;

import me.jellysquid.mods.lithium.common.world.LithiumData;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Raider.ObtainRaidLeaderBannerGoal.class)
public class PickupBannerAsLeaderGoalMixin<T extends Raider> {
    @Shadow
    @Final
    private T mob;

    // The call to Raid#getOminousBanner() is very expensive, use a cached banner during AI ticking
    @Redirect(
            method = "canUse()Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/raid/Raid;getLeaderBannerInstance(Lnet/minecraft/core/HolderGetter;)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack getOminousBanner(HolderGetter<BannerPattern> bannerPatternLookup) {
        ItemStack ominousBanner = ((LithiumData) this.mob.level()).lithium$getData().ominousBanner();
        if (ominousBanner == null) {
            ominousBanner = Raid.getLeaderBannerInstance(bannerPatternLookup);
        }
        return ominousBanner;
    }
}