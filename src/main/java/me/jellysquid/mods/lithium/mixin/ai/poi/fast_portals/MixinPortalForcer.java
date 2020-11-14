package me.jellysquid.mods.lithium.mixin.ai.poi.fast_portals;

import me.jellysquid.mods.lithium.common.world.interests.PointOfInterestStorageExtended;
import net.minecraft.block.BlockState;
import net.minecraft.class_5459;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.PortalForcer;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(PortalForcer.class)
public class MixinPortalForcer {
    @Shadow
    @Final
    private ServerWorld world;

    /**
     * @author JellySquid
     * @reason Use optimized search for nearby points, avoid slow filtering, check for valid locations first
     */
    @Overwrite
    public Optional<class_5459.class_5460> method_30483(BlockPos centerPos, boolean shrink) {
        int searchRadius = shrink ? 16 : 128;

        PointOfInterestStorage poiStorage = this.world.getPointOfInterestStorage();
        poiStorage.preloadChunks(this.world, centerPos, searchRadius);

        Optional<BlockPos> ret = ((PointOfInterestStorageExtended) poiStorage).findNearestInSquare(centerPos, searchRadius,
                PointOfInterestType.NETHER_PORTAL, PointOfInterestStorage.OccupationStatus.ANY,
                (poi) -> this.world.getBlockState(poi.getPos()).contains(Properties.HORIZONTAL_AXIS)
        );

        return ret.flatMap((pos) -> {
            BlockState state = this.world.getBlockState(pos);

            this.world.getChunkManager().addTicket(ChunkTicketType.PORTAL, new ChunkPos(pos), 3, pos);

            return Optional.of(class_5459.method_30574(pos, state.get(Properties.HORIZONTAL_AXIS), 21, Direction.Axis.Y, 21, (searchPos) ->
                    this.world.getBlockState(searchPos) == state));
        });
    }
}
