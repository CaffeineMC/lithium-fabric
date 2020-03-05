package me.jellysquid.mods.lithium.common.poi;

import me.jellysquid.mods.lithium.common.util.Collector;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.function.Predicate;

public interface IExtendedPointOfInterestSet {
    boolean get(Predicate<PointOfInterestType> type, PointOfInterestStorage.OccupationStatus status, Collector<PointOfInterest> consumer);
}