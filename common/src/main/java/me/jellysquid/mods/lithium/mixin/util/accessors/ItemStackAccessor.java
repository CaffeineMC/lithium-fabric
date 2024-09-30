package me.jellysquid.mods.lithium.mixin.util.accessors;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStack.class)
public interface ItemStackAccessor {

    @Accessor("item")
    Item lithium$getItem();
}
