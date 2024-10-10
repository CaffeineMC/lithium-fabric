package net.caffeinemc.mods.lithium.fabric.mixin.experimental.entity.block_caching.fluid_pushing;

import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.caffeinemc.mods.lithium.common.entity.block_tracking.BlockCache;
import net.caffeinemc.mods.lithium.common.entity.block_tracking.BlockCacheProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements BlockCacheProvider {
    @Shadow
    protected Object2DoubleMap<TagKey<Fluid>> fluidHeight;

    @Inject(
            method = "updateFluidHeightAndDoFluidPushing(Lnet/minecraft/tags/TagKey;D)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void skipFluidSearchUsingCache(TagKey<Fluid> fluid, double speed, CallbackInfoReturnable<Boolean> cir) {
        BlockCache bc = this.getUpdatedBlockCache((Entity) (Object) this);
        double fluidHeight = bc.getStationaryFluidHeightOrDefault(fluid, -1d);
        if (fluidHeight != -1d) {
            this.fluidHeight.put(fluid, fluidHeight); //Note: If the region is unloaded in target method, this still puts 0. However, default return value is 0, and vanilla doesn't use any method that reveals this difference.
            boolean touchingFluid = fluidHeight != 0d;
            cir.setReturnValue(touchingFluid);
        }
    }

    @Inject(
            method = "updateFluidHeightAndDoFluidPushing(Lnet/minecraft/tags/TagKey;D)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;length()D", ordinal = 0)
    )
    private void cacheFluidSearchResult(TagKey<Fluid> fluid, double speed, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 1) double fluidHeight, @Local(ordinal = 1) boolean touchingFluid, @Local Vec3 fluidPush) {
        BlockCache bc = this.lithium$getBlockCache();
        if (bc.isTracking() && fluidPush.lengthSqr() == 0d) {
            if (touchingFluid == (fluidHeight == 0d)) {
                throw new IllegalArgumentException("Expected fluid touching IFF fluid height is not 0! Fluid height: " + fluidHeight + " Touching fluid: " + touchingFluid + " Fluid Tag: " + fluid);
            }
            bc.setCachedFluidHeight(fluid, fluidHeight);
        }
    }
}
