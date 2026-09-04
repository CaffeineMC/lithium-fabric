package net.caffeinemc.mods.lithium.mixin.collections.mob_spawning;

import com.google.common.collect.Maps;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(MobSpawnSettings.class)
public class MobSpawnSettingsMixin {
    @Mutable
    @Shadow
    @Final
    private Map<MobCategory, WeightedList<MobSpawnSettings.SpawnerData>> spawnsByCategory;

    /**
     * Re-initialize the spawn category lists with a much faster backing collection type for enum keys. This provides
     * a modest speed-up for mob spawning as {@link MobSpawnSettings#getMobs(MobCategory)} is a rather hot method.
     */
    @Inject(method = "<init>(Ljava/util/Map;Ljava/util/Map;)V", at = @At("RETURN"))
    private void reinit(CallbackInfo ci) {
        Map<MobCategory, WeightedList<MobSpawnSettings.SpawnerData>> spawns = Maps.newEnumMap(MobCategory.class);

        spawns.putAll(this.spawnsByCategory);

        this.spawnsByCategory = spawns;
    }
}
