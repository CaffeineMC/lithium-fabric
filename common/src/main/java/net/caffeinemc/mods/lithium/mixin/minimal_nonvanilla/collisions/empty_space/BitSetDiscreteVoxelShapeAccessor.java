package net.caffeinemc.mods.lithium.mixin.minimal_nonvanilla.collisions.empty_space;

import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.BitSet;

@Mixin(BitSetDiscreteVoxelShape.class)
public interface BitSetDiscreteVoxelShapeAccessor {
    
    @Accessor
    BitSet getStorage();
}
