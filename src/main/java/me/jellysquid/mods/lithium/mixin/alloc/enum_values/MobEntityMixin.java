package me.jellysquid.mods.lithium.mixin.alloc.enum_values;

import me.jellysquid.mods.lithium.common.util.EquipmentSlots;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MobEntity.class)
public class MobEntityMixin {

    /**
     * Avoid cloning enum values.
     */
    @Redirect(
            method = "convertTo",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/EquipmentSlot;values()[Lnet/minecraft/entity/EquipmentSlot;"
            )
    )
    private EquipmentSlot[] removeAllocation() {
        return EquipmentSlots.ALL;
    }
}
