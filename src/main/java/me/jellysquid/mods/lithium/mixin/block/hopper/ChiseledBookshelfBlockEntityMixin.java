package me.jellysquid.mods.lithium.mixin.block.hopper;

import me.jellysquid.mods.lithium.api.inventory.LithiumTransferConditionInventory;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChiseledBookshelfBlockEntity.class)
public class ChiseledBookshelfBlockEntityMixin implements LithiumTransferConditionInventory {

    @Override
    public boolean lithium$itemInsertionTestRequiresStackSize1() {
        return true;
    }
}
