package me.jellysquid.mods.lithium.mixin.block.hopper;

import me.jellysquid.mods.lithium.api.inventory.LithiumTransferConditionInventory;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChiseledBookShelfBlockEntity.class)
public class ChiseledBookshelfBlockEntityMixin implements LithiumTransferConditionInventory {

    @Override
    public boolean lithium$itemInsertionTestRequiresStackSize1() {
        return true;
    }
}
