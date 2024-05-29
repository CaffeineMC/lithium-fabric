package me.jellysquid.mods.lithium.mixin.minimal_nonvanilla.collisions.empty_space;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ArrayVoxelShape.class)
public interface ArrayVoxelShapeInvoker {
    @Invoker(value = "<init>")
    static ArrayVoxelShape init(VoxelSet shape, DoubleList xPoints, DoubleList yPoints, DoubleList zPoints) {
        throw new AssertionError();
    }
}
