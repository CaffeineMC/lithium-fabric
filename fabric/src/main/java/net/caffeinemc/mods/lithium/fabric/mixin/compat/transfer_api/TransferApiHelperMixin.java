package net.caffeinemc.mods.lithium.fabric.mixin.compat.transfer_api;

import net.caffeinemc.mods.lithium.common.compat.TransferApiHelper;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(TransferApiHelper.class)
public class TransferApiHelperMixin {

    /**
     * @author 2No2Name
     * @reason Implement Fabric Transfer API Compatibility
     */
    @Overwrite
    public static boolean canHopperInteractWithApiInventory(HopperBlockEntity hopperBlockEntity, BlockState hopperState, boolean extracting) {
        Direction direction = extracting ? Direction.UP : hopperState.getValue(HopperBlock.FACING);
        BlockPos targetPos = hopperBlockEntity.getBlockPos().relative(direction);

        Object target = ItemStorage.SIDED.find(hopperBlockEntity.getLevel(), targetPos, direction.getOpposite());
        return target != null;
    }
}
