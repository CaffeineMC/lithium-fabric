package net.caffeinemc.mods.lithium.mixin.entity.equipment_tracking;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.caffeinemc.mods.lithium.common.entity.EquipmentEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobEntityMixin extends Entity implements EquipmentEntity {
    @Shadow
    private ItemStack bodyArmorItem;

    public MobEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @WrapOperation(
            method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;set(ILjava/lang/Object;)Ljava/lang/Object;"),
            require = 2
    )
    private <E> E trackEquipChange(NonNullList<E> list, int index, E element, Operation<E> original) {
        E prevElement = original.call(list, index, element);
        this.trackEquipChange(prevElement, element);
        return prevElement;
    }

    @Inject(
            method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V",
            at = {@At("HEAD"), @At("RETURN")}
    )
    private void trackBodyArmor(CompoundTag nbt, CallbackInfo ci, @Share("prevBodyArmor")LocalRef<ItemStack> prevBodyArmorRef) {
        ItemStack prevBodyArmor = prevBodyArmorRef.get();
        if (prevBodyArmor == null) {
            prevBodyArmorRef.set(this.bodyArmorItem);
        } else if (prevBodyArmor != this.bodyArmorItem) {
            this.trackEquipChange(prevBodyArmor, this.bodyArmorItem);
        }
    }

    @Unique
    private <E> void trackEquipChange(E prevElement, E element) {
        if ((!this.level().isClientSide()) && element instanceof ItemStack newStack && prevElement instanceof ItemStack prevStack) {
            this.lithium$onEquipmentReplaced(prevStack, newStack);
        }
    }
}
