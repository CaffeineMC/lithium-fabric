package me.jellysquid.mods.lithium.mixin.block.flatten_states;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FluidState.class)
public abstract class FluidStateMixin {
    @Shadow
    public abstract Fluid getFluid();

    private boolean isEmptyCache;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initFluidCache(Fluid fluid, Reference2ObjectArrayMap<?, ?> propertyMap, MapCodec<?> codec, CallbackInfo ci) {
        this.isEmptyCache = this.getFluid().isEmpty();
    }

    /**
     * @reason Use cached property
     * @author Maity
     */
    @Overwrite
    public boolean isEmpty() {
        return this.isEmptyCache;
    }
}
