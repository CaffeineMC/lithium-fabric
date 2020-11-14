package me.jellysquid.mods.lithium.common.world.interests;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.Optional;
import java.util.function.Predicate;

public interface PointOfInterestStorageExtended {
    /**
     * An optimized function for finding the nearest point in a square radius, calculated by the given predicate. This
     * function iterates through chunks from the center of the search radius outwards, which can speed up searches when
     * the origin is nearby to a valid point of interest.
     *
     * @param pos The origin point of the search volume
     * @param radius The radius of the search volume
     * @param type The type predicate to filter points by early on so they are not passed to {@param predicate}
     * @param status The required status of points
     * @param predicate The point predicate to filter points by once they are sorted
     * @return The first position returned by {@param function}
     */
    Optional<BlockPos> findNearestInSquare(BlockPos pos, int radius, PointOfInterestType type, PointOfInterestStorage.OccupationStatus status,
                                           Predicate<PointOfInterest> predicate);
}
