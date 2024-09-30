package me.jellysquid.mods.lithium.mixin.shapes.lazy_shape_context;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.EntityCollisionContext;

@Mixin(EntityCollisionContext.class)
public class EntityShapeContextMixin {
    @Mutable
    @Shadow
    @Final
    private ItemStack heldItem;

    @Mutable
    @Shadow
    @Final
    private Predicate<FluidState> canStandOnFluid;

    @Shadow
    @Final
    @Nullable
    private Entity entity;

    /**
     * Mixin the instanceof to always return false to avoid the expensive inventory access.
     * No need to use Opcodes.INSTANCEOF or similar.
     */
    @ModifyConstant(
            method = "<init>(Lnet/minecraft/world/entity/Entity;)V",
            constant = @Constant(classValue = LivingEntity.class, ordinal = 0)
    )
    private static boolean redirectInstanceOf(Object obj, Class<?> clazz) {
        return false;
    }

    @ModifyConstant(
            method = "<init>(Lnet/minecraft/world/entity/Entity;)V",
            constant = @Constant(classValue = LivingEntity.class, ordinal = 2)
    )
    private static boolean redirectInstanceOf2(Object obj, Class<?> clazz) {
        return false;
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/shapes/EntityCollisionContext;<init>(ZDLnet/minecraft/world/item/ItemStack;Ljava/util/function/Predicate;Lnet/minecraft/world/entity/Entity;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void initFields(Entity entity, CallbackInfo ci) {
        this.heldItem = null;
        this.canStandOnFluid = null;
    }

    @Inject(
            method = "isHoldingItem(Lnet/minecraft/world/item/Item;)Z",
            at = @At("HEAD")
    )
    public void isHolding(Item item, CallbackInfoReturnable<Boolean> cir) {
        this.initHeldItem();
    }

    @Intrinsic
    public ItemStack getHeldItem() {
        return this.heldItem;
    }

    @SuppressWarnings({"UnresolvedMixinReference", "MixinAnnotationTarget"})
    @Inject(
            method = "getHeldItem",
            at = @At("HEAD")
    )
    private void initHeldItem(CallbackInfoReturnable<ItemStack> callbackInfoReturnable) {
        this.initHeldItem();
    }

    @Unique
    private void initHeldItem() {
        if (this.heldItem == null) {
            this.heldItem = this.entity instanceof LivingEntity ? ((LivingEntity) this.entity).getMainHandItem() : ItemStack.EMPTY;
        }
    }

    @Inject(
            method = "canStandOnFluid(Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/material/FluidState;)Z",
            at = @At("HEAD")
    )
    public void canWalkOnFluid(FluidState state, FluidState fluidState, CallbackInfoReturnable<Boolean> cir) {
        if (this.canStandOnFluid == null) {
            if (this.entity instanceof LivingEntity livingEntity) {
                this.canStandOnFluid = livingEntity::canStandOnFluid;
            } else {
                this.canStandOnFluid = (liquid) -> false;
            }
        }
    }
}
