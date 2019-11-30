package me.jellysquid.mods.lithium.mixin.voxelshape.fast_shape_vertex_merging;

import net.minecraft.util.BooleanBiFunction;
import net.minecraft.util.shape.Lithium_VoxelShapes;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(VoxelShapes.class)
public abstract class MixinVoxelShapes {
    /**
     * Use a variant which is optimized to avoid bounds checks.
     *
     * @author JellySquid
     */
    @Overwrite
    public static boolean matchesAnywhere(VoxelShape shape1, VoxelShape shape2, BooleanBiFunction func) {
        return Lithium_VoxelShapes.matchesAnywhere(shape1, shape2, func);
    }
}
