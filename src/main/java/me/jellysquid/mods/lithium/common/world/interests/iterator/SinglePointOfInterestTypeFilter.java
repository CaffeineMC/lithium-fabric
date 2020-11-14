package me.jellysquid.mods.lithium.common.world.interests.iterator;

import net.minecraft.world.poi.PointOfInterestType;

import java.util.function.Predicate;

public class SinglePointOfInterestTypeFilter implements Predicate<PointOfInterestType> {
    private final PointOfInterestType type;

    public SinglePointOfInterestTypeFilter(PointOfInterestType type) {
        this.type = type;
    }

    @Override
    public boolean test(PointOfInterestType other) {
        return this.type == other;
    }

    public PointOfInterestType getType() {
        return this.type;
    }
}
