package me.jellysquid.mods.lithium.common.block;

import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

/**
 * A replacement for EntityShapeContext that does not calculate the heldItem on construction. As most instances never
 * use this field, a lazy evaluation is faster on average. The initialization of the heldItem field takes about 1% of
 * the server thread CPU time in a fresh world with lots of animals (singleplayer 1.16.5, renderdistance 24).
 *
 * @author 2No2Name
 */
public class LithiumEntityShapeContext implements ShapeContext {
    private final Entity entity;
    private final boolean descending;
    private final double minY;
    private Item heldItem;

    public LithiumEntityShapeContext(Entity entity) {
        this.entity = entity;
        this.descending = entity.isDescending();
        this.minY = entity.getY();
    }

    @Override
    public boolean isHolding(Item item) {
        if (this.heldItem == null) {
            this.heldItem = entity instanceof LivingEntity ? ((LivingEntity)entity).getMainHandStack().getItem() : Items.AIR;
        }
        return this.heldItem == item;
    }

    @Override
    public boolean method_27866(FluidState aboveState, FlowableFluid fluid) {
        return this.entity instanceof LivingEntity && ((LivingEntity) this.entity).canWalkOnFluid(fluid) && !aboveState.getFluid().matchesType(fluid);
    }

    @Override
    public boolean isDescending() {
        return this.descending;
    }

    @Override
    public boolean isAbove(VoxelShape shape, BlockPos pos, boolean defaultValue) {
        return this.minY > (double)pos.getY() + shape.getMax(Direction.Axis.Y) - 9.999999747378752E-6D;
    }
}
