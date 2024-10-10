package net.caffeinemc.mods.lithium.mixin.world.raycast;

import net.minecraft.world.level.ClipContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClipContext.class)
public interface ClipContextAccessor {

    @Accessor("fluid")
    ClipContext.Fluid getFluidHandling();
}
