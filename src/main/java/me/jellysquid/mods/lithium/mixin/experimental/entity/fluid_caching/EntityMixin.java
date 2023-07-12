package me.jellysquid.mods.lithium.mixin.experimental.entity.fluid_caching;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import me.jellysquid.mods.lithium.common.entity.block_tracking.FluidListeningInfo;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public abstract Box getBoundingBox();

    @Shadow
    protected Object2DoubleMap<TagKey<Fluid>> fluidHeight;

    @Shadow
    public abstract World getWorld();

    private FluidListeningInfo fluidCache;

    @Inject(
            method = "updateMovementInFluid",
            at = @At("HEAD"),
            cancellable = true
    )
    private void skipFluidSearchUsingCache(TagKey<Fluid> tag, double speed, CallbackInfoReturnable<Boolean> cir) {
        if (this.fluidCache == null) {
            this.fluidCache = new FluidListeningInfo();
        }
        this.fluidCache.updateTracker(this.getBoundingBox(), this.getWorld());
        if (this.fluidCache.cachedIsNotTouchingFluid(tag)) {
            this.fluidHeight.put(tag, 0.0D);
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "updateMovementInFluid",
            at = @At("RETURN")
    )
    private void cacheFluidSearchResult(TagKey<Fluid> tag, double speed, CallbackInfoReturnable<Boolean> cir) {
        boolean touchedFluid = cir.getReturnValueZ();
        if (!touchedFluid && this.fluidCache != null) {
            this.fluidCache.cacheNotTouchingFluid(tag, this.getWorld().getTime());
        }
    }

    @Inject(
            method = "remove",
            at = @At("HEAD")
    )
    private void removeFluidCache(Entity.RemovalReason reason, CallbackInfo ci) {
        if (this.fluidCache != null) {
            this.fluidCache.remove();
            this.fluidCache = null;
        }
    }
}
