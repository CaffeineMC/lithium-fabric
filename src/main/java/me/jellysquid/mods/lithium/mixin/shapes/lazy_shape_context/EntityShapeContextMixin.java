package me.jellysquid.mods.lithium.mixin.shapes.lazy_shape_context;

import net.minecraft.block.EntityShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(EntityShapeContext.class)
public class EntityShapeContextMixin {
    @Mutable
    @Shadow
    @Final
    private Item heldItem;
    @Mutable
    @Shadow
    @Final
    private Predicate<Fluid> field_24425;

    private Entity lithium_entity;

    /**
     * Mixin the instanceof to always return false to avoid the expensive inventory access.
     * No need to use Opcodes.INSTANCEOF or similar.
     */
    @ModifyConstant(
            method = "<init>(Lnet/minecraft/entity/Entity;)V",
            constant = @Constant(classValue = LivingEntity.class, ordinal = 0)
    )
    private static boolean redirectInstanceOf(Object obj, Class<?> clazz) {
        return false;
    }

    @ModifyConstant(
            method = "<init>(Lnet/minecraft/entity/Entity;)V",
            constant = @Constant(classValue = LivingEntity.class, ordinal = 2)
    )
    private static boolean redirectInstanceOf2(Object obj, Class<?> clazz) {
        return false;
    }

    @Inject(
            method = "<init>(Lnet/minecraft/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/EntityShapeContext;<init>(ZDLnet/minecraft/item/Item;Ljava/util/function/Predicate;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void initFields(Entity entity, CallbackInfo ci) {
        this.heldItem = null;
        this.field_24425 = null;
        this.lithium_entity = entity;
    }

    /**
     * @author 2No2Name
     * @reason allow skipping unused initialization
     */
    @Overwrite
    public boolean isHolding(Item item) {
        if (this.heldItem == null) {
            this.heldItem = this.lithium_entity instanceof LivingEntity ? ((LivingEntity)this.lithium_entity).getMainHandStack().getItem() : Items.AIR;
        }
        return this.heldItem == item;
    }

    /**
     * @author 2No2Name
     * @reason allow skipping unused lambda allocation
     */
    @Overwrite
    public boolean method_27866(FluidState aboveState, FlowableFluid fluid) {
        return this.lithium_entity instanceof LivingEntity && ((LivingEntity) this.lithium_entity).canWalkOnFluid(fluid) && !aboveState.getFluid().matchesType(fluid);
    }
}
