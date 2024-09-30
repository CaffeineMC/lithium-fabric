package me.jellysquid.mods.lithium.mixin.block.hopper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import net.minecraft.core.NonNullList;

@Mixin(NonNullList.class)
public interface DefaultedListAccessor<T> {
    @Accessor("list")
    List<T> getDelegate();
}
