package net.caffeinemc.mods.lithium.common.compat;

import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TransferApiHelper {

    // Overwrite in TransferApiHelperMixin
    public static boolean canHopperInteractWithApiInventory(HopperBlockEntity hopperBlockEntity, BlockState hopperState, boolean extracting) {
        return false;
    }
}
