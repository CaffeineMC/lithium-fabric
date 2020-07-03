package me.jellysquid.mods.lithium.mixin.shapes.blockstate_cache;

import me.jellysquid.mods.lithium.common.block.BlockShapeCacheExtended;
import me.jellysquid.mods.lithium.common.block.BlockShapeHelper;
import net.minecraft.block.BlockState;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.EmptyBlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Extends the ShapeCache to contain some additional properties as to avoid expensive computation from some Redstone
 * components which look to see if a block's shape can support it. This prompted an issue to be opened on the Mojang
 * issue tracker, which contains some additional information: https://bugs.mojang.com/browse/MC-174568
 */
@Mixin(BlockState.ShapeCache.class)
public class BlockShapeCacheMixin implements BlockShapeCacheExtended {
    private static final Direction[] DIRECTIONS = Direction.values();

    @Shadow
    @Final
    protected boolean[] solidFullSquare;

    @Shadow
    @Final
    protected boolean isFullCube;

    private byte sideCoversSmallSquare;
    private boolean hasTopRim;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(BlockState state, CallbackInfo ci) {
        // [VanillaCopy] Leaf blocks are a special case which can never support other blocks
        // This is exactly how vanilla itself implements the check.
        if (!state.isIn(BlockTags.LEAVES)) {
            this.initSidedProperties(state);
        }
    }

    private void initSidedProperties(BlockState state) {
        VoxelShape shape = state.getSidesShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);

        // If the shape is a full cube and the top face is a full square, it can always support another component
        this.hasTopRim = (this.isFullCube && this.solidFullSquare[Direction.UP.ordinal()]) ||
                BlockShapeHelper.sideCoversSquare(shape.getFace(Direction.UP), BlockShapeHelper.SOLID_MEDIUM_SQUARE_SHAPE);

        for (Direction side : DIRECTIONS) {
            // [VanillaCopy] Block#sideCoversSmallSquare
            if (side == Direction.DOWN && state.isIn(BlockTags.UNSTABLE_BOTTOM_CENTER)) {
                continue;
            }

            if (this.solidFullSquare[side.ordinal()] || BlockShapeHelper.sideCoversSquare(shape.getFace(side), BlockShapeHelper.SOLID_SMALL_SQUARE_SHAPE)) {
                this.sideCoversSmallSquare |= (1 << side.ordinal());
            }
        }
    }

    @Override
    public boolean sideCoversSmallSquare(Direction facing) {
        return (this.sideCoversSmallSquare & (1 << facing.ordinal())) != 0;
    }

    @Override
    public boolean hasTopRim() {
        return this.hasTopRim;
    }
}
