package me.jellysquid.mods.lithium.mixin.minimal_nonvanilla.collisions.empty_space;

import net.minecraft.util.shape.BitSetVoxelSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.BitSet;

@Mixin(BitSetVoxelSet.class)
public interface BitSetVoxelSetAccessor {
    
    @Accessor
    BitSet getStorage();
}
