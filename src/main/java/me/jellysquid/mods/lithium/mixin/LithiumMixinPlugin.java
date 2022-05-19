package me.jellysquid.mods.lithium.mixin;

import net.caffeinemc.caffeineconfig.AbstractCaffeineConfigMixinPlugin;
import net.caffeinemc.caffeineconfig.CaffeineConfig;
import net.fabricmc.loader.api.FabricLoader;

public class LithiumMixinPlugin extends AbstractCaffeineConfigMixinPlugin {
    private static final String MIXIN_PACKAGE_ROOT = "me.jellysquid.mods.lithium.mixin.";

    @Override
    protected CaffeineConfig createConfig() {
        // Defines the default rules which can be configured by the user or other mods.
        // You must manually add a rule for any new mixins not covered by an existing package rule.
        return CaffeineConfig.builder("Lithium")
                .addMixinOption("ai", true)
                .addMixinOption("ai.nearby_entity_tracking", true)
                .addMixinOption("ai.nearby_entity_tracking.goals", true)
                .addMixinOption("ai.pathing", true)
                .addMixinOption("ai.poi", true)
                .addMixinOption("ai.poi.fast_portals", true)
                .addMixinOption("ai.poi.poi.tasks", true)
                .addMixinOption("ai.raid", true)
                .addMixinOption("ai.sensor", true)
                .addMixinOption("ai.sensor.secondary_poi", true)
                .addMixinOption("ai.task", true)
                .addMixinOption("ai.task.fast_repetition", false) //removed during 1.18 update
                .addMixinOption("ai.task.goat_jump", true)
                .addMixinOption("ai.task.launch", true)
                .addMixinOption("ai.task.memory_change_counting", true)
                .addMixinOption("ai.task.replace_streams", true)

                .addMixinOption("alloc", true)
                .addMixinOption("alloc.blockstate", true)
                .addMixinOption("alloc.chunk_random", true)
                .addMixinOption("alloc.chunk_ticking", true)
                .addMixinOption("alloc.composter", true)
                .addMixinOption("alloc.deep_passengers", true)
                .addMixinOption("alloc.entity_tracker", true)
                .addMixinOption("alloc.enum_values", true)
                .addMixinOption("alloc.explosion_behavior", true)
                .addMixinOption("alloc.nbt", true)

                .addMixinOption("block", true)
                .addMixinOption("block.flatten_states", true)
                .addMixinOption("block.hopper", true)
                .addMixinOption("block.moving_block_shapes", true)
                .addMixinOption("block.redstone_wire", true)

                .addMixinOption("cached_hashcode", true)

                .addMixinOption("chunk", true)
                .addMixinOption("chunk.block_counting", true)
                .addMixinOption("chunk.entity_class_groups", true)
                .addMixinOption("chunk.no_locking", true)
                .addMixinOption("chunk.no_validation", true)
                .addMixinOption("chunk.palette", true)
                .addMixinOption("chunk.serialization", true)

                .addMixinOption("collections", true)
                .addMixinOption("collections.attributes", true)
                .addMixinOption("collections.brain", true)
                .addMixinOption("collections.entity_by_type", true)
                .addMixinOption("collections.entity_filtering", true)
                .addMixinOption("collections.entity_ticking", true)
                .addMixinOption("collections.gamerules", true)
                .addMixinOption("collections.goals", true)
                .addMixinOption("collections.mob_spawning", true)

                .addMixinOption("entity", true)
                .addMixinOption("entity.collisions", true)
                .addMixinOption("entity.collisions.fluid", true)
                .addMixinOption("entity.collisions.intersection", true)
                .addMixinOption("entity.collisions.movement", true)
                .addMixinOption("entity.collisions.suffocation", true)
                .addMixinOption("entity.collisions.unpushable_cramming", true)
                .addMixinOption("entity.data_tracker", true)
                .addMixinOption("entity.data_tracker.no_locks", true)
                .addMixinOption("entity.data_tracker.use_arrays", true)
                .addMixinOption("entity.fast_elytra_check", true)
                .addMixinOption("entity.fast_hand_swing", true)
                .addMixinOption("entity.fast_powder_snow_check", true)
                .addMixinOption("entity.fast_retrieval", true)
                .addMixinOption("entity.inactive_navigations", true)
                .addMixinOption("entity.replace_entitytype_predicates", true)
                .addMixinOption("entity.skip_equipment_change_check", true)
                .addMixinOption("entity.skip_fire_check", true)

                .addMixinOption("gen", true)
                .addMixinOption("gen.biome_noise_cache", false) //removed during 1.18 update
                .addMixinOption("gen.cached_generator_settings", true)
                .addMixinOption("gen.chunk_region", true)
                .addMixinOption("gen.fast_island_noise", false) //removed during 1.18 update
                .addMixinOption("gen.fast_layer_sampling", false) //removed during 1.18 update
                .addMixinOption("gen.fast_multi_source_biomes", false) //removed during 1.18 update
                .addMixinOption("gen.features", false) //removed during 1.18 update
                .addMixinOption("gen.perlin_noise", false) //removed during 1.18 update

                .addMixinOption("item", true)

                .addMixinOption("math", true)
                .addMixinOption("math.fast_blockpos", true)
                .addMixinOption("math.fast_util", true)
                .addMixinOption("math.sine_lut", true)

                .addMixinOption("profiler", true)

                .addMixinOption("shapes", true)
                .addMixinOption("shapes.blockstate_cache", true)
                .addMixinOption("shapes.lazy_shape_context", true)
                .addMixinOption("shapes.optimized_matching", true)
                .addMixinOption("shapes.precompute_shape_arrays", true)
                .addMixinOption("shapes.shape_merging", true)
                .addMixinOption("shapes.specialized_shapes", true)

                .addMixinOption("util", true)
                .addMixinOption("util.entity_section_position", true)

                .addMixinOption("world", true)
                .addMixinOption("world.block_entity_retrieval", true)
                .addMixinOption("world.block_entity_ticking", true)
                .addMixinOption("world.block_entity_ticking.support_cache", false) //have to check whether the cached state bugfix fixes any detectable vanilla bugs first
                .addMixinOption("world.chunk_access", true)
                .addMixinOption("world.chunk_tickets", true)
                .addMixinOption("world.chunk_ticking", true)
                .addMixinOption("world.explosions", true)
                .addMixinOption("world.inline_block_access", true)
                .addMixinOption("world.inline_height", true)
                .addMixinOption("world.player_chunk_tick", true)
                .addMixinOption("world.tick_scheduler", false) //removed during 1.18 update

                .addOptionDependency("ai.nearby_entity_tracking", "util", true)
                .addOptionDependency("ai.nearby_entity_tracking", "util.entity_section_position", true)
                .addOptionDependency("block.hopper", "ai", true)
                .addOptionDependency("block.hopper", "ai.nearby_entity_tracking", true)
                .addOptionDependency("block.hopper", "world", true)
                .addOptionDependency("block.hopper", "world.block_entity_retrieval", true)

                .addOptionDependency("entity.collisions.fluid", "chunk", true)
                .addOptionDependency("entity.collisions.fluid", "chunk.block_counting", true)
                .withInfoUrl("https://github.com/CaffeineMC/lithium-fabric/wiki/Configuration-File")
                .build(FabricLoader.getInstance().getConfigDir().resolve("lithium.properties"));
    }

    @Override
    protected String mixinPackageRoot() {
        return MIXIN_PACKAGE_ROOT;
    }
}
