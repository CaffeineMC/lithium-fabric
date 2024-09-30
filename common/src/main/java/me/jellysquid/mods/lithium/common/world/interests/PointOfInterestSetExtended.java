package me.jellysquid.mods.lithium.common.world.interests;

import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;

public interface PointOfInterestSetExtended {
    void lithium$collectMatchingPoints(Predicate<Holder<PoiType>> type, PoiManager.Occupancy status,
                                       Consumer<PoiRecord> consumer);
}