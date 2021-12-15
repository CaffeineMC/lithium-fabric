package me.jellysquid.mods.lithium.mixin.alloc.enum_values;

import me.jellysquid.mods.lithium.common.util.EquipmentSlotConstants;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ZombieVillagerEntity.class)
public class ZombieVillagerEntityMixin {

    @Redirect(
            method = "finishConversion",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/EquipmentSlot;values()[Lnet/minecraft/entity/EquipmentSlot;"
            )
    )
    private EquipmentSlot[] removeAllocation() {
        return EquipmentSlotConstants.ALL;
    }
}
