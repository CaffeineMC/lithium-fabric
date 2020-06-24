package me.jellysquid.mods.lithium.mixin.shapes.specialized_shapes;

import me.jellysquid.mods.lithium.common.shapes.VoxelShapeEmpty;
import me.jellysquid.mods.lithium.common.shapes.VoxelShapeSimpleCube;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.BitSetVoxelSet;
import net.minecraft.util.shape.VoxelSet;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.spongepowered.asm.mixin.*;

/**
 * Shape specialization allows us to optimize comparison logic by guaranteeing certain constraints about the
 * configuration of vertices in a given shape. For example, most block shapes consist of only one cuboid and by
 * nature, only one voxel. This fact can be taken advantage of to create an optimized implementation which avoids
 * scanning over voxels as there are only ever two given vertices in the shape, allowing simple math operations to be
 * used for determining intersection and penetration.
 * <p>
 * In most cases, comparison logic is rather simple as the game often only deals with empty shapes or simple cubes.
 * Specialization provides a significant speed-up to entity collision resolution and various other parts of the game
 * without needing invasive patches, as we can simply replace the types returned by this class. Modern processors
 * (along with the help of the potent JVM) make the cost of dynamic dispatch negligible when compared to the execution
 * times of shape comparison methods.
 */
@Mixin(VoxelShapes.class)
public abstract class MixinVoxelShapes {
    @Mutable
    @Shadow
    @Final
    public static VoxelShape UNBOUNDED;

    @Mutable
    @Shadow
    @Final
    private static VoxelShape FULL_CUBE;

    @Mutable
    @Shadow
    @Final
    private static VoxelShape EMPTY;

    private static final VoxelSet FULL_CUBE_VOXELS;

    // Re-initialize the global cached shapes with our specialized ones. This will happen right after all the static
    // state has been initialized and before any external classes access it.
    static {
        // [VanillaCopy] The FULL_CUBE and UNBOUNDED shape is initialized with a single 1x1x1 voxel as neither will
        // contain multiple inner cuboids.
        FULL_CUBE_VOXELS = new BitSetVoxelSet(1, 1, 1);
        FULL_CUBE_VOXELS.set(0, 0, 0, true, true);

        // Used in some rare cases to indicate a shape which encompasses the entire world (such as a moving world border)
        UNBOUNDED = new VoxelShapeSimpleCube(FULL_CUBE_VOXELS, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

        // Represents a full-block cube shape, such as that for a dirt block.
        FULL_CUBE = new VoxelShapeSimpleCube(FULL_CUBE_VOXELS, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0);

        // Represents an empty cube shape with no vertices that cannot be collided with.
        EMPTY = new VoxelShapeEmpty(new BitSetVoxelSet(0, 0, 0));
    }

    /**
     * Vanilla implements some very complex logic in this function in order to allow entity boxes to be used in
     * collision resolution the same way as block shapes. The specialized simple cube shape however can trivially
     * represent these cases with nothing more than the two vertexes. This provides a modest speed up for entity
     * collision code by allowing them to also use our optimized shapes.
     *
     * @reason Use our optimized shape types
     * @author JellySquid
     */
    @Overwrite
    public static VoxelShape cuboid(Box box) {
        return new VoxelShapeSimpleCube(FULL_CUBE_VOXELS, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
    }
}
