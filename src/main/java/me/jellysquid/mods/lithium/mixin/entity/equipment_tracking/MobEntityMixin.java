package me.jellysquid.mods.lithium.mixin.entity.equipment_tracking;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import me.jellysquid.mods.lithium.common.entity.EquipmentEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends Entity implements EquipmentEntity {
    @Shadow
    private ItemStack bodyArmor;

    public MobEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @WrapOperation(
            method = "readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;set(ILjava/lang/Object;)Ljava/lang/Object;"),
            require = 2
    )
    private <E> E trackEquipChange(DefaultedList<E> list, int index, E element, Operation<E> original) {
        E prevElement = original.call(list, index, element);
        this.trackEquipChange(prevElement, element);
        return prevElement;
    }

    @Inject(
            method = "readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V",
            at = {@At("HEAD"), @At("RETURN")}
    )
    private void trackBodyArmor(NbtCompound nbt, CallbackInfo ci, @Share("prevBodyArmor")LocalRef<ItemStack> prevBodyArmorRef) {
        ItemStack prevBodyArmor = prevBodyArmorRef.get();
        if (prevBodyArmor == null) {
            prevBodyArmorRef.set(this.bodyArmor);
        } else if (prevBodyArmor != this.bodyArmor) {
            this.trackEquipChange(prevBodyArmor, this.bodyArmor);
        }
    }

    @Unique
    private <E> void trackEquipChange(E prevElement, E element) {
        if ((!this.getWorld().isClient()) && element instanceof ItemStack newStack && prevElement instanceof ItemStack prevStack) {
            this.lithium$onEquipmentReplaced(prevStack, newStack);
        }
    }
}
