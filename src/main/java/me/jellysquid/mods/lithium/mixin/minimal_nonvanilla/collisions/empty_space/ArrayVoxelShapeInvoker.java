package me.jellysquid.mods.lithium.mixin.minimal_nonvanilla.collisions.empty_space;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.world.phys.shapes.ArrayVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ArrayVoxelShape.class)
public interface ArrayVoxelShapeInvoker {
    @Invoker(value = "<init>")
    static ArrayVoxelShape init(DiscreteVoxelShape shape, DoubleList xPoints, DoubleList yPoints, DoubleList zPoints) {
        throw new AssertionError();
    }
}
