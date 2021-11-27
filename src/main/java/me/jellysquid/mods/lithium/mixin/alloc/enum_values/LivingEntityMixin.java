package me.jellysquid.mods.lithium.mixin.alloc.enum_values;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    private static final EquipmentSlot[] SLOTS = EquipmentSlot.values();

    /**
     * @reason Avoid cloning enum values
     */
    @Redirect(
            method = "getEquipmentChanges()Ljava/util/Map;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/EquipmentSlot;values()[Lnet/minecraft/entity/EquipmentSlot;"
            )
    )
    private EquipmentSlot[] redirectEquipmentSlotsClone() {
        return SLOTS;
    }
}
