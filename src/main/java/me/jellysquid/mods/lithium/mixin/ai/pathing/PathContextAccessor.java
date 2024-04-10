package me.jellysquid.mods.lithium.mixin.ai.pathing;

import net.minecraft.entity.ai.pathing.PathContext;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PathContext.class)
public interface PathContextAccessor {

    @Accessor
    BlockPos.Mutable getLastNodePos();

}
