package me.jellysquid.mods.lithium.common.util.tuples;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;

public record SortedPointOfInterest(PoiRecord poi, double distanceSq) {

    public SortedPointOfInterest(PoiRecord poi, BlockPos origin) {
        this(poi, poi.getPos().distSqr(origin));
    }

    public BlockPos getPos() {
        return this.poi.getPos();
    }

    public int getX() {
        return this.getPos().getX();
    }

    public int getY() {
        return this.getPos().getY();
    }

    public int getZ() {
        return this.getPos().getZ();
    }
}
