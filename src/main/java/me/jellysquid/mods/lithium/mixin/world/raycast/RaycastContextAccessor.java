package me.jellysquid.mods.lithium.mixin.world.raycast;

import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RaycastContext.class)
public interface RaycastContextAccessor {

    @Accessor("fluid")
    RaycastContext.FluidHandling getFluidHandling();
}
