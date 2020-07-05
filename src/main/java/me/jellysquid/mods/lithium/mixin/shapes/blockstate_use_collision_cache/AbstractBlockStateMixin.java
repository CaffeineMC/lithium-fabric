package me.jellysquid.mods.lithium.mixin.shapes.blockstate_use_collision_cache;

import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin {
    @Shadow
    public abstract Block getBlock();

    @Shadow
    protected abstract BlockState asBlockState();


    @Shadow
    public AbstractBlock.AbstractBlockState.ShapeCache shapeCache;

    /**
     * @author 2No2Name
     * @reason Use the shapeCache when it is available.
     */
    @Overwrite
    public VoxelShape getCollisionShape(BlockView world, BlockPos pos, ShapeContext context) {
        //LAVA has the property that its VoxelShape depends on the ShapeContext.
        //Usually blocks like that are marked as dynamicBounds() and the shapeCache is not initialized.
        //For some reason this is not the case for LAVA, therefore checking for shapeCache != null is not enough
        return this.shapeCache != null && this.getBlock() != Blocks.LAVA ?
                this.shapeCache.collisionShape : this.getBlock().getCollisionShape(this.asBlockState(), world, pos, context);
    }

    /**
     * @author 2No2Name
     * @reason Avoid the additional checks introduced by the overwrite above. this.shapeCache is already known to be null here.
     */
    @Redirect(method = "getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/shape/VoxelShape;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/AbstractBlock$AbstractBlockState;getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;"))
    public VoxelShape getCollisionShape(AbstractBlock.AbstractBlockState abstractBlockState, BlockView world, BlockPos pos, ShapeContext context) {
        return this.getBlock().getCollisionShape(this.asBlockState(), world, pos, context);
    }
}
