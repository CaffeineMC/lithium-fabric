package me.jellysquid.mods.lithium.mixin.block.hopper;

import net.minecraft.block.entity.Hopper;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * @author Leon Camus
 * @since 07.01.2021
 */
@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {
    private static final Box hopperBox = new Box(0.0D, 11.0D, 0.0D, 16.0D, 32.0D, 16.0D);

    /**
     * @reason The hoppers collision shape already prohibits items to reach the occluded shape. Reduces entity search overhead by 50% and removes the need to merge lists.
     * @author 28Smiles
     */
    @Redirect(
            method = "extract",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/block/entity/HopperBlockEntity;getInputItemEntities(Lnet/minecraft/block/entity/Hopper;)Ljava/util/List;",
                    shift = At.Shift.BEFORE,
                    ordinal = 0
            )
    )
    private static List<ItemEntity> getInputItemEntities(Hopper hopper) {
        return hopper.getWorld().getEntitiesByClass(
                ItemEntity.class,
                hopperBox.offset(hopper.getHopperX() - 0.5D, hopper.getHopperY() - 0.5D, hopper.getHopperZ() - 0.5D),
                EntityPredicates.VALID_ENTITY
        );
    }
}
