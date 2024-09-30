package me.jellysquid.mods.lithium.mixin.block.hopper;

import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CompoundContainer.class)
public interface DoubleInventoryAccessor {

    @Accessor("container1")
    Container getFirst();

    @Accessor("container2")
    Container getSecond();
}
