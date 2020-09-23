package me.jellysquid.mods.lithium.mixin.block.piston_shapes;

import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * The piston block computes a new VoxelShape every time the shape is requested, causing significant overhead when
 * anything queries it. Additionally, it renders caches with reference equality semantics in the game useless as every
 * shape returned will be a different instance.
 * <p>
 * There are only 12 unique shapes for a piston head, being a short and long variant for each orientation. As such, it
 * is fairly inconsequential to cache these all in a lookup array.
 */
@Mixin(PistonHeadBlock.class)
public class PistonHeadBlockMixin {
    @Shadow
    @Final
    protected static VoxelShape DOWN_HEAD_SHAPE;

    @Shadow
    @Final
    protected static VoxelShape UP_HEAD_SHAPE;

    @Shadow
    @Final
    protected static VoxelShape NORTH_HEAD_SHAPE;

    @Shadow
    @Final
    protected static VoxelShape SOUTH_HEAD_SHAPE;

    @Shadow
    @Final
    protected static VoxelShape WEST_HEAD_SHAPE;

    @Shadow
    @Final
    protected static VoxelShape EAST_HEAD_SHAPE;

    @Shadow
    @Final
    protected static VoxelShape SHORT_DOWN_ARM_SHAPE;

    @Shadow
    @Final
    protected static VoxelShape SHORT_UP_ARM_SHAPE;

    @Shadow
    @Final
    protected static VoxelShape SHORT_NORTH_ARM_SHAPE;

    @Shadow
    @Final
    protected static VoxelShape SHORT_SOUTH_ARM_SHAPE;

    @Shadow
    @Final
    protected static VoxelShape SHORT_WEST_ARM_SHAPE;

    @Shadow
    @Final
    protected static VoxelShape SHORT_EAST_ARM_SHAPE;

    @Shadow
    @Final
    protected static VoxelShape DOWN_ARM_SHAPE;

    @Shadow
    @Final
    protected static VoxelShape UP_ARM_SHAPE;

    @Shadow
    @Final
    protected static VoxelShape NORTH_ARM_SHAPE;

    @Shadow
    @Final
    protected static VoxelShape SOUTH_ARM_SHAPE;

    @Shadow
    @Final
    protected static VoxelShape WEST_ARM_SHAPE;

    @Shadow
    @Final
    protected static VoxelShape EAST_ARM_SHAPE;

    @Shadow
    @Final
    public static BooleanProperty SHORT;

    private static final VoxelShape[][] outlineShapes;
    private static final int SHORT_IDX = 0, LONG_IDX = 1;

    static {
        outlineShapes = new VoxelShape[6][];
        outlineShapes[Direction.DOWN.ordinal()] = createShape(DOWN_HEAD_SHAPE, SHORT_DOWN_ARM_SHAPE, DOWN_ARM_SHAPE);
        outlineShapes[Direction.UP.ordinal()] = createShape(UP_HEAD_SHAPE, SHORT_UP_ARM_SHAPE, UP_ARM_SHAPE);
        outlineShapes[Direction.NORTH.ordinal()] = createShape(NORTH_HEAD_SHAPE, SHORT_NORTH_ARM_SHAPE, NORTH_ARM_SHAPE);
        outlineShapes[Direction.SOUTH.ordinal()] = createShape(SOUTH_HEAD_SHAPE, SHORT_SOUTH_ARM_SHAPE, SOUTH_ARM_SHAPE);
        outlineShapes[Direction.WEST.ordinal()] = createShape(WEST_HEAD_SHAPE, SHORT_WEST_ARM_SHAPE, WEST_ARM_SHAPE);
        outlineShapes[Direction.EAST.ordinal()] = createShape(EAST_HEAD_SHAPE, SHORT_EAST_ARM_SHAPE, EAST_ARM_SHAPE);

    }

    private static VoxelShape[] createShape(VoxelShape head, VoxelShape shortArm, VoxelShape arm) {
        VoxelShape[] shapes = new VoxelShape[2];
        shapes[SHORT_IDX] = VoxelShapes.union(head, shortArm);
        shapes[LONG_IDX] = VoxelShapes.union(head, arm);

        return shapes;
    }

    /**
     * @reason Use cached shape
     * @author JellySquid
     */
    @Overwrite
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        return outlineShapes[state.get(FacingBlock.FACING).ordinal()][state.get(SHORT) ? SHORT_IDX : LONG_IDX];
    }
}
