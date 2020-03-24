package me.jellysquid.mods.lithium.common.world.interests;

import me.jellysquid.mods.lithium.common.util.Collector;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestSet;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.function.Predicate;

public class PointOfInterestActions {
    public static Collector<PointOfInterest> collectAllWithinRadius(BlockPos pos, double radius, Collector<PointOfInterest> out) {
        double radiusSq = radius * radius;

        return (point) -> {
            if (point.getPos().getSquaredDistance(pos) <= radiusSq) {
                return out.collect(point);
            }

            return true;
        };
    }

    public static Collector<PointOfInterestSet> collectAllMatching(Predicate<PointOfInterestType> predicate, PointOfInterestStorage.OccupationStatus status, Collector<PointOfInterest> out) {
        return (set) -> ((ExtendedPointOfInterestSet) set).get(predicate, status, out);
    }
}
