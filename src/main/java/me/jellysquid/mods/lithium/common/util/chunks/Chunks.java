package me.jellysquid.mods.lithium.common.util.chunks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;

public class Chunks {
    public static Inventory getInventoryInChunk(WorldChunk chunk, BlockPos pos) {
        Inventory inventory = null;
        BlockState blockState = chunk.getBlockState(pos);
        Block block = blockState.getBlock();
        if (block instanceof InventoryProvider) {
            inventory = ((InventoryProvider)block).getInventory(blockState, chunk.getWorld(), pos);
        } else if (block.hasBlockEntity()) {
            BlockEntity blockEntity = chunk.getBlockEntity(pos);
            if (blockEntity instanceof Inventory) {
                inventory = (Inventory)blockEntity;
                if (inventory instanceof ChestBlockEntity && block instanceof ChestBlock)
                    inventory = ChestBlock.getInventory(blockState, chunk.getWorld(), pos, true);
            }
        }

        if (inventory == null) {
            List<Entity> list = new ArrayList<>();
            chunk.appendEntities((Entity) null, new Box(pos.getX() - 0.5D, pos.getY() - 0.5D, pos.getZ() - 0.5D, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D), list, EntityPredicates.VALID_INVENTORIES);
            if (!list.isEmpty()) {
                inventory = (Inventory)list.get(chunk.getWorld().random.nextInt(list.size()));
            }
        }

        return inventory;
    }
}
