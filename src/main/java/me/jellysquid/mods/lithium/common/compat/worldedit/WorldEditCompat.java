package me.jellysquid.mods.lithium.common.compat.worldedit;

import me.jellysquid.mods.lithium.common.hopper.UpdateReceiver;
import me.jellysquid.mods.lithium.common.util.DirectionConstants;
import me.jellysquid.mods.lithium.common.world.WorldHelper;
import me.jellysquid.mods.lithium.common.world.blockentity.BlockEntityGetter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Objects;

public class WorldEditCompat {

    public static final boolean WORLD_EDIT_PRESENT = FabricLoader.getInstance().isModLoaded("worldedit");

    public static void updateHopperCachesOnNewInventoryAdded(WorldChunk worldChunk, BlockEntity blockEntity) {
        World world = Objects.requireNonNull(blockEntity.getWorld());
        BlockPos pos = Objects.requireNonNull(blockEntity.getPos());

        BlockPos.Mutable neighborPos = new BlockPos.Mutable();
        for (Direction offsetDirection : DirectionConstants.ALL) {
            neighborPos.set(pos, offsetDirection);
            BlockEntity neighborBlockEntity =
                    WorldHelper.arePosWithinSameChunk(pos, neighborPos) ?
                            worldChunk.getBlockEntity(neighborPos, WorldChunk.CreationType.CHECK) :
                            ((BlockEntityGetter) world).getLoadedExistingBlockEntity(neighborPos);
            if (neighborBlockEntity instanceof UpdateReceiver updateReceiver) {
                updateReceiver.invalidateCacheOnNeighborUpdate(offsetDirection.getOpposite());
            }
        }
    }
}
