package net.caffeinemc.mods.lithium.common.world.interests;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.border.WorldBorder;

public interface PointOfInterestStorageExtended {
    /**
     * An optimized function for finding the nearest (L2 distance) point inside a square radius. This
     * function iterates through chunks from the center of the search radius outwards, which can speed up searches when
     * the origin is nearby to a valid point of interest.
     * <p>
     * Order of the POI:
     * return closest accepted POI (L2 distance). If several exist:
     * return the one with most negative Y. If several exist:
     * return the one with most negative X. If several exist:
     * return the one with most negative Z. If several exist: Be confused about two POIs being in the same location.
     *
     * @param pos                The origin point of the search volume
     * @param radius             The radius of the search volume
     * @param type               The type predicate to filter points by early on so they are not passed to {@param predicate}
     * @param status             The required status of points
     * @param afterSortPredicate The point predicate to filter points by once they are sorted
     * @param worldBorder        The world border the POI must be inside.
     * @return The first accepted position (respecting the described order)
     */
    Optional<PoiRecord> lithium$findNearestForPortalLogic(BlockPos pos, int radius, Holder<PoiType> type, PoiManager.Occupancy status,
                                                                Predicate<PoiRecord> afterSortPredicate, WorldBorder worldBorder);
}
