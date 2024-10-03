package net.caffeinemc.mods.lithium.mixin.block.hopper;

import net.minecraft.core.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(NonNullList.class)
public interface NonNullListAccessor<T> {
    @Accessor("list")
    List<T> getDelegate();
}
