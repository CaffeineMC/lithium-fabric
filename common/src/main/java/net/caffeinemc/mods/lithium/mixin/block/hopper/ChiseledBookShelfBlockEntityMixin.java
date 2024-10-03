package net.caffeinemc.mods.lithium.mixin.block.hopper;

import net.caffeinemc.mods.lithium.api.inventory.LithiumTransferConditionInventory;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChiseledBookShelfBlockEntity.class)
public class ChiseledBookShelfBlockEntityMixin implements LithiumTransferConditionInventory {

    @Override
    public boolean lithium$itemInsertionTestRequiresStackSize1() {
        return true;
    }
}
