package me.jellysquid.mods.lithium.mixin.ai.poi.poi.tasks;

import me.jellysquid.mods.lithium.common.util.POIRegistryEntries;
import me.jellysquid.mods.lithium.common.world.interests.iterator.SinglePointOfInterestTypeFilter;
import net.minecraft.entity.ai.brain.task.HideInHomeTask;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;
import java.util.function.Predicate;

@Mixin(HideInHomeTask.class)
public class HideInHomeTaskMixin {

    @Redirect(
            method = "run(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LivingEntity;J)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/poi/PointOfInterestStorage;getPosition(Ljava/util/function/Predicate;Ljava/util/function/Predicate;Lnet/minecraft/world/poi/PointOfInterestStorage$OccupationStatus;Lnet/minecraft/util/math/BlockPos;ILnet/minecraft/util/math/random/Random;)Ljava/util/Optional;"
            )
    )
    private Optional<BlockPos> redirect(PointOfInterestStorage instance, Predicate<RegistryEntry<PointOfInterestType>> typePredicate, Predicate<BlockPos> positionPredicate, PointOfInterestStorage.OccupationStatus occupationStatus, BlockPos pos, int radius, Random random) {
        return instance.getPosition(new SinglePointOfInterestTypeFilter(POIRegistryEntries.HOME_ENTRY), positionPredicate, occupationStatus, pos, radius, random);
    }
}
