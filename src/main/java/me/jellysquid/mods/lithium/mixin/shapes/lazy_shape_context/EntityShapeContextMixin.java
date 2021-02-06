package me.jellysquid.mods.lithium.mixin.shapes.lazy_shape_context;

import net.minecraft.block.EntityShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Predicate;

@Mixin(EntityShapeContext.class)
public class EntityShapeContextMixin {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Shadow
    @Final
    private Optional<Entity> entity;

    @Mutable
    @Shadow
    @Final
    private ItemStack heldItem;

    @Mutable
    @Shadow
    @Final
    private Predicate<Fluid> walkOnFluidPredicate;

    @Mutable
    @Shadow
    @Final
    private ItemStack boots;

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

    @ModifyConstant(
            method = "<init>(Lnet/minecraft/entity/Entity;)V",
            constant = @Constant(classValue = LivingEntity.class, ordinal = 4)
    )
    private static boolean redirectInstanceOf3(Object obj, Class<?> clazz) {
        return false;
    }

    @Inject(
            method = "<init>(Lnet/minecraft/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/EntityShapeContext;<init>(ZDLnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Ljava/util/function/Predicate;Ljava/util/Optional;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void initFields(Entity entity, CallbackInfo ci) {
        this.heldItem = null;
        this.walkOnFluidPredicate = null;
        this.boots = null;
    }

    /**
     * @author 2No2Name
     * @reason allow skipping unused initialization
     */
    @Overwrite
    public boolean isHolding(Item item) {
        if (this.heldItem == null) {
            this.heldItem = this.entity.isPresent() && this.entity.get() instanceof LivingEntity ? ((LivingEntity)this.entity.get()).getMainHandStack() : ItemStack.EMPTY;
        }
        return this.heldItem.isOf(item);
    }


    /**
     * @author 2No2Name
     * @reason allow skipping unused lambda allocation
     */
    @Overwrite
    public boolean canWalkOnFluid(FluidState aboveState, FlowableFluid fluid) {
        return this.entity.isPresent() && this.entity.get() instanceof LivingEntity && ((LivingEntity) this.entity.get()).canWalkOnFluid(fluid) && !aboveState.getFluid().matchesType(fluid);
    }
}
