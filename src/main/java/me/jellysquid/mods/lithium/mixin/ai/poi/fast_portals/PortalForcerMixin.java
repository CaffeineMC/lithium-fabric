package me.jellysquid.mods.lithium.mixin.ai.poi.fast_portals;

import me.jellysquid.mods.lithium.common.util.POIRegistryEntries;
import me.jellysquid.mods.lithium.common.world.interests.PointOfInterestStorageExtended;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.PortalForcer;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(PortalForcer.class)
public class PortalForcerMixin {
    @Shadow
    @Final
    private ServerWorld world;

    /**
     * @author JellySquid, 2No2Name
     * @reason Use optimized search for nearby points, avoid slow filtering, check for valid locations first
     * [VanillaCopy] everything but the Optional<PointOfInterest> lookup
     */
    @Overwrite
    public Optional<BlockPos> getPortalPos(BlockPos centerPos, boolean dstIsNether, WorldBorder worldBorder) {
        int searchRadius = dstIsNether ? 16 : 128;

        PointOfInterestStorage poiStorage = this.world.getPointOfInterestStorage();
        poiStorage.preloadChunks(this.world, centerPos, searchRadius);

        Optional<PointOfInterest> ret = ((PointOfInterestStorageExtended) poiStorage).lithium$findNearestForPortalLogic(centerPos, searchRadius,
                POIRegistryEntries.NETHER_PORTAL_ENTRY, PointOfInterestStorage.OccupationStatus.ANY,
                (poi) -> this.world.getBlockState(poi.getPos()).contains(Properties.HORIZONTAL_AXIS),
                worldBorder
        );

        return ret.map(PointOfInterest::getPos);
    }
}
