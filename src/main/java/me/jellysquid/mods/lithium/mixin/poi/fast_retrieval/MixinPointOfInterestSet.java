package me.jellysquid.mods.lithium.mixin.poi.fast_retrieval;

import me.jellysquid.mods.lithium.common.world.interests.ExtendedPointOfInterestSet;
import me.jellysquid.mods.lithium.common.util.Collector;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestSet;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

@Mixin(PointOfInterestSet.class)
public class MixinPointOfInterestSet implements ExtendedPointOfInterestSet {
    @Shadow
    @Final
    private Map<PointOfInterestType, Set<PointOfInterest>> pointsOfInterestByType;

    @Override
    public boolean get(Predicate<PointOfInterestType> type, PointOfInterestStorage.OccupationStatus status, Collector<PointOfInterest> consumer) {
        for (Map.Entry<PointOfInterestType, Set<PointOfInterest>> entry : this.pointsOfInterestByType.entrySet()) {
            if (!type.test(entry.getKey())) {
                continue;
            }

            for (PointOfInterest poi : entry.getValue()) {
                if (!status.getPredicate().test(poi)) {
                    continue;
                }

                if (!consumer.collect(poi)) {
                    return false;
                }
            }
        }

        return true;
    }
}
