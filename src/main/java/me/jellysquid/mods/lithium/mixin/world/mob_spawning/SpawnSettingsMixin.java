package me.jellysquid.mods.lithium.mixin.world.mob_spawning;

import com.google.common.collect.Maps;
import me.jellysquid.mods.lithium.common.util.collections.HashedReferenceList;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.world.biome.SpawnSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(SpawnSettings.class)
public class SpawnSettingsMixin {
    @Mutable
    @Shadow
    @Final
    private Map<SpawnGroup, List<SpawnSettings.SpawnEntry>> spawners;

    /**
     * Re-initialize the spawn category lists with a much faster backing collection type for enum keys. This provides
     * a modest speed-up for mob spawning as {@link SpawnSettings#getSpawnEntries(SpawnGroup)} is a rather hot method.
     * <p>
     * Additionally, the list containing each spawn entry is modified to include a hash table for lookups, making them
     * O(1) instead of O(n) and providing another boost when lists get large. Since a simple wrapper type is used, this
     * should provide good compatibility with other mods which modify spawn entries.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void reinit(float creatureSpawnProbability, Map<SpawnGroup, List<SpawnSettings.SpawnEntry>> spawners, Map<EntityType<?>, SpawnSettings.SpawnDensity> spawnCosts, boolean playerSpawnFriendly, CallbackInfo ci) {
        Map<SpawnGroup, List<SpawnSettings.SpawnEntry>> spawns = Maps.newEnumMap(SpawnGroup.class);

        for (Map.Entry<SpawnGroup, List<SpawnSettings.SpawnEntry>> entry : this.spawners.entrySet()) {
            spawns.put(entry.getKey(), new HashedReferenceList<>(entry.getValue()));
        }

        this.spawners = spawns;
    }
}
