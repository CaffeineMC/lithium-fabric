package me.jellysquid.mods.lithium.mixin.minimal_nonvanilla.collisions.empty_space;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.BitSet;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;

@Mixin(BitSetDiscreteVoxelShape.class)
public interface BitSetVoxelSetAccessor {
    
    @Accessor
    BitSet getStorage();
}
