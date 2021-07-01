package me.jellysquid.mods.lithium.common.config;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.CustomValue.CvType;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Documentation of these options: https://github.com/jellysquid3/lithium-fabric/wiki/Configuration-File
 */
@SuppressWarnings("CanBeFinal")
public class LithiumConfig {
    private static final Logger LOGGER = LogManager.getLogger("LithiumConfig");

    private static final String JSON_KEY_LITHIUM_OPTIONS = "lithium:options";

    private final Map<String, Option> options = new HashMap<>();
    private final Set<Option> optionsWithDependencies = new ObjectLinkedOpenHashSet<>();

    private LithiumConfig() {
        // Defines the default rules which can be configured by the user or other mods.
        // You must manually add a rule for any new mixins not covered by an existing package rule.

        this.addMixinRule("ai", true);
        this.addMixinRule("ai.brain", true);
        this.addMixinRule("ai.goal", true);
        this.addMixinRule("ai.nearby_entity_tracking", true);
        this.addMixinRule("ai.nearby_entity_tracking.goals", true);
        this.addMixinRule("ai.pathing", true);
        this.addMixinRule("ai.poi", true);
        this.addMixinRule("ai.poi.fast_init", true);
        this.addMixinRule("ai.poi.fast_retrieval", true);
        this.addMixinRule("ai.raid", true);
        this.addMixinRule("ai.task", true);

        this.addMixinRule("alloc", true);
        this.addMixinRule("alloc.chunk_random", true);
        this.addMixinRule("alloc.chunk_ticking", true);
        this.addMixinRule("alloc.composter", true);
        this.addMixinRule("alloc.entity_tracker", true);
        this.addMixinRule("alloc.enum_values", true);
        this.addMixinRule("alloc.explosion_behavior", true);

        this.addMixinRule("block", true);
        this.addMixinRule("block.flatten_states", true);
        this.addMixinRule("block.hopper", true);
        this.addMixinRule("block.moving_block_shapes", true);

        this.addMixinRule("cached_hashcode", true);

        this.addMixinRule("chunk", true);
        this.addMixinRule("chunk.count_oversized_blocks", true);
        this.addMixinRule("chunk.entity_class_groups", true);
        this.addMixinRule("chunk.no_locking", true);
        this.addMixinRule("chunk.palette", true);
        this.addMixinRule("chunk.section_update_tracking", true);
        this.addMixinRule("chunk.serialization", true);

        this.addMixinRule("collections", true);
        this.addMixinRule("collections.entity_filtering", true);

        this.addMixinRule("entity", true);
        this.addMixinRule("entity.collisions", true);
        this.addMixinRule("entity.data_tracker", true);
        this.addMixinRule("entity.data_tracker.no_locks", true);
        this.addMixinRule("entity.data_tracker.use_arrays", true);
        this.addMixinRule("entity.fast_retrieval", true);
        this.addMixinRule("entity.fast_suffocation_check", true);
        this.addMixinRule("entity.gravity_check_block_below", true);
        this.addMixinRule("entity.inactive_navigations", true);
        this.addMixinRule("entity.replace_entitytype_predicates", true);
        this.addMixinRule("entity.skip_fire_check", true);
        this.addMixinRule("entity.stream_entity_collisions_lazily", true);

        this.addMixinRule("gen", true);
        this.addMixinRule("gen.biome_noise_cache", true);
        this.addMixinRule("gen.cached_generator_settings", true);
        this.addMixinRule("gen.chunk_region", true);
        this.addMixinRule("gen.fast_island_noise", true);
        this.addMixinRule("gen.fast_layer_sampling", true);
        this.addMixinRule("gen.fast_multi_source_biomes", true);
        this.addMixinRule("gen.features", true);
        this.addMixinRule("gen.perlin_noise", true);
        this.addMixinRule("gen.voronoi_biomes", true);

        this.addMixinRule("item", true);

        this.addMixinRule("math", true);
        this.addMixinRule("math.fast_blockpos", true);
        this.addMixinRule("math.fast_util", true);
        this.addMixinRule("math.sine_lut", true);

        this.addMixinRule("shapes", true);
        this.addMixinRule("shapes.blockstate_cache", true);
        this.addMixinRule("shapes.lazy_shape_context", true);
        this.addMixinRule("shapes.optimized_matching", true);
        this.addMixinRule("shapes.precompute_shape_arrays", true);
        this.addMixinRule("shapes.shape_merging", true);
        this.addMixinRule("shapes.specialized_shapes", true);

        this.addMixinRule("tag", true);

        this.addMixinRule("world", true);
        this.addMixinRule("world.block_entity_retrieval", true);
        this.addMixinRule("world.block_entity_ticking", true);
        this.addMixinRule("world.block_entity_ticking.support_cache", false); //have to check whether the cached state bugfix fixes any detectable vanilla bugs first
        this.addMixinRule("world.chunk_access", true);
        this.addMixinRule("world.chunk_inline_block_access", true);
        this.addMixinRule("world.chunk_task_system", true);
        this.addMixinRule("world.chunk_tickets", true);
        this.addMixinRule("world.chunk_ticking", true);
        this.addMixinRule("world.explosions", true);
        this.addMixinRule("world.mob_spawning", true);
        this.addMixinRule("world.player_chunk_tick", true);
        this.addMixinRule("world.tick_scheduler", true);

        this.addRuleDependency("block.hopper", "ai", true);
        this.addRuleDependency("block.hopper", "ai.nearby_entity_tracking", true);
        this.addRuleDependency("block.hopper", "world", true);
        this.addRuleDependency("block.hopper", "world.block_entity_retrieval", true);
    }

    /**
     * Loads the configuration file from the specified location. If it does not exist, a new configuration file will be
     * created. The file on disk will then be updated to include any new options.
     */
    public static LithiumConfig load(File file) {
        LithiumConfig config = new LithiumConfig();

        if (file.exists()) {
            Properties props = new Properties();

            try (FileInputStream fin = new FileInputStream(file)) {
                props.load(fin);
            } catch (IOException e) {
                throw new RuntimeException("Could not load config file", e);
            }

            config.readProperties(props);
        } else {
            try {
                writeDefaultConfig(file);
            } catch (IOException e) {
                LOGGER.warn("Could not write default configuration file", e);
            }
        }

        config.applyModOverrides();

        // Check dependencies several times, because one iteration may disable a rule required by another rule
        // This terminates because each additional iteration will disable one or more rules, and there is only a finite number of rules
        while (config.applyDependencies()) {
            ;
        }

        return config;
    }

    /**
     * Defines a dependency between two registered mixin rules. If a dependency is not satisfied, the mixin will
     * be disabled.
     *
     * @param rule          the mixin rule that requires another rule to be set to a given value
     * @param dependency    the mixin rule the given rule depends on
     * @param requiredValue the required value of the dependency
     */
    @SuppressWarnings("SameParameterValue")
    private void addRuleDependency(String rule, String dependency, boolean requiredValue) {
        String ruleOptionName = getMixinRuleName(rule);
        Option option = this.options.get(ruleOptionName);
        if (option == null) {
            LOGGER.error("Option {} for dependency '{} depends on {}={}' not found. Skipping.", rule, rule, dependency, requiredValue);
            return;
        }
        String dependencyOptionName = getMixinRuleName(dependency);
        Option dependencyOption = this.options.get(dependencyOptionName);
        if (dependencyOption == null) {
            LOGGER.error("Option {} for dependency '{} depends on {}={}' not found. Skipping.", dependency, rule, dependency, requiredValue);
            return;
        }
        option.addDependency(dependencyOption, requiredValue);
        this.optionsWithDependencies.add(option);
    }


    /**
     * Defines a Mixin rule which can be configured by users and other mods.
     *
     * @param mixin   The name of the mixin package which will be controlled by this rule
     * @param enabled True if the rule will be enabled by default, otherwise false
     * @throws IllegalStateException If a rule with that name already exists
     */
    private void addMixinRule(String mixin, boolean enabled) {
        String name = getMixinRuleName(mixin);

        if (this.options.putIfAbsent(name, new Option(name, enabled, false)) != null) {
            throw new IllegalStateException("Mixin rule already defined: " + mixin);
        }
    }

    private void readProperties(Properties props) {
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            Option option = this.options.get(key);

            if (option == null) {
                LOGGER.warn("No configuration key exists with name '{}', ignoring", key);
                continue;
            }

            boolean enabled;

            if (value.equalsIgnoreCase("true")) {
                enabled = true;
            } else if (value.equalsIgnoreCase("false")) {
                enabled = false;
            } else {
                LOGGER.warn("Invalid value '{}' encountered for configuration key '{}', ignoring", value, key);
                continue;
            }

            option.setEnabled(enabled, true);
        }
    }

    private void applyModOverrides() {
        for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
            ModMetadata meta = container.getMetadata();

            if (meta.containsCustomValue(JSON_KEY_LITHIUM_OPTIONS)) {
                CustomValue overrides = meta.getCustomValue(JSON_KEY_LITHIUM_OPTIONS);

                if (overrides.getType() != CvType.OBJECT) {
                    LOGGER.warn("Mod '{}' contains invalid Lithium option overrides, ignoring", meta.getId());
                    continue;
                }

                for (Map.Entry<String, CustomValue> entry : overrides.getAsObject()) {
                    this.applyModOverride(meta, entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private void applyModOverride(ModMetadata meta, String name, CustomValue value) {
        Option option = this.options.get(name);

        if (option == null) {
            LOGGER.warn("Mod '{}' attempted to override option '{}', which doesn't exist, ignoring", meta.getId(), name);
            return;
        }

        if (value.getType() != CvType.BOOLEAN) {
            LOGGER.warn("Mod '{}' attempted to override option '{}' with an invalid value, ignoring", meta.getId(), name);
            return;
        }

        boolean enabled = value.getAsBoolean();

        // disabling the option takes precedence over enabling
        if (!enabled && option.isEnabled()) {
            option.clearModsDefiningValue();
        }

        if (!enabled || option.isEnabled() || option.getDefiningMods().isEmpty()) {
            option.addModOverride(enabled, meta.getId());
        }
    }

    /**
     * Returns the effective option for the specified class name. This traverses the package path of the given mixin
     * and checks each root for configuration rules. If a configuration rule disables a package, all mixins located in
     * that package and its children will be disabled. The effective option is that of the highest-priority rule, either
     * a enable rule at the end of the chain or a disable rule at the earliest point in the chain.
     *
     * @return Null if no options matched the given mixin name, otherwise the effective option for this Mixin
     */
    public Option getEffectiveOptionForMixin(String mixinClassName) {
        int lastSplit = 0;
        int nextSplit;

        Option rule = null;

        while ((nextSplit = mixinClassName.indexOf('.', lastSplit)) != -1) {
            String key = getMixinRuleName(mixinClassName.substring(0, nextSplit));

            Option candidate = this.options.get(key);

            if (candidate != null) {
                rule = candidate;

                if (!rule.isEnabled()) {
                    return rule;
                }
            }

            lastSplit = nextSplit + 1;
        }

        return rule;
    }

    /**
     * Tests all dependencies and disables options when their dependencies are not met.
     */
    private boolean applyDependencies() {
        boolean changed = false;
        for (Option optionWithDependency : this.optionsWithDependencies) {
            changed |= optionWithDependency.disableIfDependenciesNotMet(LOGGER);
        }
        return changed;
    }

    private static void writeDefaultConfig(File file) throws IOException {
        File dir = file.getParentFile();

        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Could not create parent directories");
            }
        } else if (!dir.isDirectory()) {
            throw new IOException("The parent file is not a directory");
        }

        try (Writer writer = new FileWriter(file)) {
            writer.write("# This is the configuration file for Lithium.\n");
            writer.write("# This file exists for debugging purposes and should not be configured otherwise.\n");
            writer.write("#\n");
            writer.write("# You can find information on editing this file and all the available options here:\n");
            writer.write("# https://github.com/jellysquid3/lithium-fabric/wiki/Configuration-File\n");
            writer.write("#\n");
            writer.write("# By default, this file will be empty except for this notice.\n");
        }
    }

    private static String getMixinRuleName(String name) {
        return "mixin." + name;
    }

    public int getOptionCount() {
        return this.options.size();
    }

    public int getOptionOverrideCount() {
        return (int) this.options.values()
                .stream()
                .filter(Option::isOverridden)
                .count();
    }
}
