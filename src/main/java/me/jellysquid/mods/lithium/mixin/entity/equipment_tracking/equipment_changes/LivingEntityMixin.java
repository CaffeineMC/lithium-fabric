package me.jellysquid.mods.lithium.mixin.entity.equipment_tracking.equipment_changes;

import me.jellysquid.mods.lithium.common.entity.EquipmentEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements EquipmentEntity.EquipmentTrackingEntity {

    @Unique
    private boolean equipmentChanged = true;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public void lithium$onEquipmentChanged() {
        this.equipmentChanged = true;
    }

    @Inject(
            method = "getEquipmentChanges()Ljava/util/Map;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void skipSentEquipmentComparison(CallbackInfoReturnable<@Nullable Map<EquipmentSlot, ItemStack>> cir) {
        if (!this.equipmentChanged) {
            cir.setReturnValue(null);
        }
    }

    @Inject(
            method = "sendEquipmentChanges()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;checkHandStackSwap(Ljava/util/Map;)V"
            )
    )
    private void resetEquipmentChanged(CallbackInfo ci) {
        //Not implemented for player entities.
        //noinspection ConstantValue
        if (!((Object) this instanceof PlayerEntity)) {
            this.equipmentChanged = false;
        }
    }
}
