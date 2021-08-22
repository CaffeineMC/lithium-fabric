package me.jellysquid.mods.lithium.mixin.shapes.lazy_shape_context;

import net.minecraft.block.EntityShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Inject(
            method = "isWearingOnFeet(Lnet/minecraft/item/Item;)Z",
            at = @At("HEAD")
    )
    public void isWearingOnFeet(Item item, CallbackInfoReturnable<Boolean> cir) {
        if (this.boots == null) {
            this.boots = this.entity.isPresent() && this.entity.get() instanceof LivingEntity ? ((LivingEntity) this.entity.get()).getEquippedStack(EquipmentSlot.FEET) : ItemStack.EMPTY;
        }
    }

    @Inject(
            method = "isHolding(Lnet/minecraft/item/Item;)Z",
            at = @At("HEAD")
    )
    public void isHolding(Item item, CallbackInfoReturnable<Boolean> cir) {
        if (this.heldItem == null) {
            this.heldItem = this.entity.isPresent() && this.entity.get() instanceof LivingEntity ? ((LivingEntity) this.entity.get()).getMainHandStack() : ItemStack.EMPTY;
        }
    }

    @Inject(
            method = "canWalkOnFluid(Lnet/minecraft/fluid/FluidState;Lnet/minecraft/fluid/FlowableFluid;)Z",
            at = @At("HEAD")
    )
    public void canWalkOnFluid(FluidState state, FlowableFluid fluid, CallbackInfoReturnable<Boolean> cir) {
        if (this.walkOnFluidPredicate == null) {
            Entity entity = this.entity.orElse(null);
            if (entity instanceof LivingEntity livingEntity) {
                this.walkOnFluidPredicate = livingEntity::canWalkOnFluid;
            } else {
                this.walkOnFluidPredicate = (liquid) -> false;
            }
        }
    }
}
