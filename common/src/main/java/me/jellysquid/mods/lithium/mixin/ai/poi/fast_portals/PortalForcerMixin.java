package me.jellysquid.mods.lithium.mixin.ai.poi.fast_portals;

import me.jellysquid.mods.lithium.common.util.POIRegistryEntries;
import me.jellysquid.mods.lithium.common.world.interests.PointOfInterestStorageExtended;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.PortalForcer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(PortalForcer.class)
public class PortalForcerMixin {
    @Shadow
    @Final
    private ServerLevel level;

    /**
     * @author JellySquid, 2No2Name
     * @reason Use optimized search for nearby points, avoid slow filtering, check for valid locations first
     * [VanillaCopy] everything but the Optional<PointOfInterest> lookup
     */
    @Overwrite
    public Optional<BlockPos> findClosestPortalPosition(BlockPos centerPos, boolean dstIsNether, WorldBorder worldBorder) {
        int searchRadius = dstIsNether ? 16 : 128;

        PoiManager poiStorage = this.level.getPoiManager();
        poiStorage.ensureLoadedAndValid(this.level, centerPos, searchRadius);

        Optional<PoiRecord> ret = ((PointOfInterestStorageExtended) poiStorage).lithium$findNearestForPortalLogic(centerPos, searchRadius,
                POIRegistryEntries.NETHER_PORTAL_ENTRY, PoiManager.Occupancy.ANY,
                (poi) -> this.level.getBlockState(poi.getPos()).hasProperty(BlockStateProperties.HORIZONTAL_AXIS),
                worldBorder
        );

        return ret.map(PoiRecord::getPos);
    }
}
