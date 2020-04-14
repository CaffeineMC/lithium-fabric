package me.jellysquid.mods.lithium.mixin.block.flatten_states;

import com.google.common.collect.ImmutableMap;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.FluidStateImpl;
import net.minecraft.state.property.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The methods in {@link FluidState} involve a lot of indirection through the BlockState/Fluid classes and require
 * property lookups in order to compute the returned value. This shows up as a hot spot in some areas (namely fluid
 * ticking and world generation).
 * <p>
 * Since these are constant for any given fluid state, we can cache them nearby for improved performance and eliminate
 * the overhead.
 */
@Mixin(FluidStateImpl.class)
public abstract class MixinFluidStateImpl implements FluidState {
    private float height;
    private int level;
    private boolean isEmpty;
    private boolean isStill;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(Fluid fluid, ImmutableMap<Property<?>, Comparable<?>> properties, CallbackInfo ci) {
        this.isEmpty = fluid.isEmpty();

        this.level = fluid.getLevel(this);
        this.height = fluid.getHeight(this);
        this.isStill = fluid.isStill(this);
    }

    @Override
    public boolean isStill() {
        return this.isStill;
    }

    @Override
    public boolean isEmpty() {
        return this.isEmpty;
    }

    @Override
    public float getHeight() {
        return this.height;
    }

    @Override
    public int getLevel() {
        return this.level;
    }
}
