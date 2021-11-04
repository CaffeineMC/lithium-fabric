package me.jellysquid.mods.lithium.mixin;

import net.caffeinemc.caffeineconfig.AbstractCaffeineConfigMixinPlugin;
import net.caffeinemc.caffeineconfig.CaffeineConfig;
import net.fabricmc.loader.api.FabricLoader;

public class LithiumMixinPlugin extends AbstractCaffeineConfigMixinPlugin {
    private static final String MIXIN_PACKAGE_ROOT = "me.jellysquid.mods.lithium.mixin.";

    @Override
    protected CaffeineConfig createConfig() {
        return CaffeineConfig.builder("Lithium")
                .addMixinOption("ai", true)
                .addMixinOption("ai.brain", true)
                .addMixinOption("ai.goal", true)
                .addMixinOption("ai.nearby_entity_tracking", true)
                .addMixinOption("ai.nearby_entity_tracking.goals", true)
                .addMixinOption("ai.pathing", true)
                .addMixinOption("ai.poi", true)
                .addMixinOption("ai.poi.fast_init", true)
                .addMixinOption("ai.poi.fast_retrieval", true)
                .addMixinOption("ai.raid", true)
                .addMixinOption("ai.task", true)
                .addMixinOption("ai.task.fast_repetition", true)
                .addMixinOption("ai.task.goat_jump", true)
                .addMixinOption("ai.task.replace_streams", true)

                .addMixinOption("alloc", true)
                .addMixinOption("alloc.chunk_random", true)
                .addMixinOption("alloc.chunk_ticking", true)
                .addMixinOption("alloc.composter", true)
                .addMixinOption("alloc.deep_passenger", true)
                .addMixinOption("alloc.entity_tracker", true)
                .addMixinOption("alloc.enum_values", true)
                .addMixinOption("alloc.explosion_behavior", true)

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
                .addMixinOption("chunk.section_update_tracking", true)
                .addMixinOption("chunk.serialization", true)

                .addMixinOption("collections", true)
                .addMixinOption("collections.entity_filtering", true)

                .addMixinOption("entity", true)
                .addMixinOption("entity.collisions", true)
                .addMixinOption("entity.data_tracker", true)
                .addMixinOption("entity.data_tracker.no_locks", true)
                .addMixinOption("entity.data_tracker.use_arrays", true)
                .addMixinOption("entity.fast_retrieval", true)
                .addMixinOption("entity.fast_suffocation_check", true)
                .addMixinOption("entity.gravity_check_block_below", true)
                .addMixinOption("entity.inactive_navigations", true)
                .addMixinOption("entity.replace_entitytype_predicates", true)
                .addMixinOption("entity.skip_fire_check", true)
                .addMixinOption("entity.stream_entity_collisions_lazily", true)

                .addMixinOption("gen", true)
                .addMixinOption("gen.biome_noise_cache", true)
                .addMixinOption("gen.cached_generator_settings", true)
                .addMixinOption("gen.chunk_region", true)
                .addMixinOption("gen.fast_island_noise", true)
                .addMixinOption("gen.fast_layer_sampling", true)
                .addMixinOption("gen.fast_multi_source_biomes", true)
                .addMixinOption("gen.features", true)
                .addMixinOption("gen.perlin_noise", true)

                .addMixinOption("item", true)

                .addMixinOption("math", true)
                .addMixinOption("math.fast_blockpos", true)
                .addMixinOption("math.fast_util", true)
                .addMixinOption("math.sine_lut", true)

                .addMixinOption("shapes", true)
                .addMixinOption("shapes.blockstate_cache", true)
                .addMixinOption("shapes.lazy_shape_context", true)
                .addMixinOption("shapes.optimized_matching", true)
                .addMixinOption("shapes.precompute_shape_arrays", true)
                .addMixinOption("shapes.shape_merging", true)
                .addMixinOption("shapes.specialized_shapes", true)

                .addMixinOption("tag", true)

                .addMixinOption("world", true)
                .addMixinOption("world.block_entity_retrieval", true)
                .addMixinOption("world.block_entity_ticking", true)
                .addMixinOption("world.block_entity_ticking.support_cache", false) //have to check whether the cached state bugfix fixes any detectable vanilla bugs first
                .addMixinOption("world.chunk_access", true)
                .addMixinOption("world.chunk_task_system", true)
                .addMixinOption("world.chunk_tickets", true)
                .addMixinOption("world.chunk_ticking", true)
                .addMixinOption("world.explosions", true)
                .addMixinOption("world.inline_block_access", true)
                .addMixinOption("world.inline_height", true)
                .addMixinOption("world.mob_spawning", true)
                .addMixinOption("world.player_chunk_tick", true)
                .addMixinOption("world.tick_scheduler", true)

                .addMixinOption("worldfixer", true)

                .addOptionDependency("block.hopper", "ai", true)
                .addOptionDependency("block.hopper", "ai.nearby_entity_tracking", true)
                .addOptionDependency("block.hopper", "world", true)
                .addOptionDependency("block.hopper", "world.block_entity_retrieval", true)
                .withInfoUrl("https://github.com/CaffeineMC/lithium-fabric/wiki/Configuration-File")
                .build(FabricLoader.getInstance().getConfigDir().resolve("lithium.properties"));
    }

    @Override
    protected String mixinPackageRoot() {
        return MIXIN_PACKAGE_ROOT;
    }
}
