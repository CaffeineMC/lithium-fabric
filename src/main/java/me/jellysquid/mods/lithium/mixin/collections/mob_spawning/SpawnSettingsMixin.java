package me.jellysquid.mods.lithium.mixin.collections.mob_spawning;

import com.google.common.collect.Maps;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;

@Mixin(MobSpawnSettings.class)
public class SpawnSettingsMixin {
    @Mutable
    @Shadow
    @Final
    private Map<MobCategory, WeightedRandomList<MobSpawnSettings.SpawnerData>> spawners;

    /**
     * Re-initialize the spawn category lists with a much faster backing collection type for enum keys. This provides
     * a modest speed-up for mob spawning as {@link MobSpawnSettings#getMobs(MobCategory)} is a rather hot method.
     */
    @Inject(method = "<init>(FLjava/util/Map;Ljava/util/Map;)V", at = @At("RETURN"))
    private void reinit(float creatureSpawnProbability, Map<MobCategory, WeightedRandomList<MobSpawnSettings.SpawnerData>> spawners, Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> spawnCosts, CallbackInfo ci) {
        Map<MobCategory, WeightedRandomList<MobSpawnSettings.SpawnerData>> spawns = Maps.newEnumMap(MobCategory.class);

        for (Map.Entry<MobCategory, WeightedRandomList<MobSpawnSettings.SpawnerData>> entry : this.spawners.entrySet()) {
            spawns.put(entry.getKey(), entry.getValue());
        }

        this.spawners = spawns;
    }
}
