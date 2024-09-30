package me.jellysquid.mods.lithium.common.compat.fabric_transfer_api_v1;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FabricTransferApiCompat {
    public static final boolean FABRIC_TRANSFER_API_V_1_PRESENT;

    static {
        FABRIC_TRANSFER_API_V_1_PRESENT = FabricLoader.getInstance().isModLoaded("fabric-transfer-api-v1");
    }

    public static boolean canHopperInteractWithApiInventory(HopperBlockEntity hopperBlockEntity, BlockState hopperState, boolean extracting) {
        Direction direction = extracting ? Direction.UP : hopperState.getValue(HopperBlock.FACING);
        BlockPos targetPos = hopperBlockEntity.getBlockPos().relative(direction);

        //noinspection UnstableApiUsage
        Object target = ItemStorage.SIDED.find(hopperBlockEntity.getLevel(), targetPos, direction.getOpposite());
        return target != null;
    }
}
