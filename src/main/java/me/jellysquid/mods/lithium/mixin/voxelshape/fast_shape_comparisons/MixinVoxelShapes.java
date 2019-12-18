package me.jellysquid.mods.lithium.mixin.voxelshape.fast_shape_comparisons;

import net.minecraft.util.BooleanBiFunction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(VoxelShapes.class)
public class MixinVoxelShapes {
    /**
     * Responsible for determining whether or not a shape occludes light
     * @reason Avoid the expensive shape combination
     * @author JellySquid
     */
    @Overwrite
    public static boolean unionCoversFullCube(VoxelShape a, VoxelShape b) {
        // At least one shape is a full cube and will match
        if (a == VoxelShapes.fullCube() || b == VoxelShapes.fullCube()) {
            return true;
        }

        boolean ae = a == VoxelShapes.empty() || a.isEmpty();
        boolean be = b == VoxelShapes.empty() || b.isEmpty();

        if (ae && be) {
            return false;
        } else {
            // Test each shape individually if they're non-empty and fail fast
            if (!ae && VoxelShapes.matchesAnywhere(VoxelShapes.fullCube(), a, BooleanBiFunction.ONLY_FIRST)) {
                return false;
            }

            return be || !VoxelShapes.matchesAnywhere(VoxelShapes.fullCube(), b, BooleanBiFunction.ONLY_FIRST);
        }
    }
}
