package me.jellysquid.mods.lithium.mixin.ai.task.fast_repetition;

import me.jellysquid.mods.lithium.common.util.collections.PredicateFilterableList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.sensor.NearestLivingEntitiesSensor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Mixin(NearestLivingEntitiesSensor.class)
public class NearestLivingEntitiesSensorMixin {
    @Redirect(
            method = "sense(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/stream/Collectors;toList()Ljava/util/stream/Collector;"
            )
    )
    private Collector<LivingEntity, ?, List<LivingEntity>> collectToCustomListType() {
        return Collectors.toCollection(PredicateFilterableList::new);
    }
}
