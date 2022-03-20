package me.jellysquid.mods.lithium.common.world.interests.iterator;

import net.minecraft.world.poi.PointOfInterestType;

import java.util.function.Predicate;

public record SinglePointOfInterestTypeFilter(PointOfInterestType type) implements Predicate<PointOfInterestType> {

    @Override
    public boolean test(PointOfInterestType other) {
        return this.type == other;
    }

    public PointOfInterestType getType() {
        return this.type;
    }
}
