package me.jellysquid.mods.lithium.common.config;

import com.moandjiezana.toml.Toml;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Documentation of these options: https://github.com/jellysquid3/lithium-fabric/wiki/Configuration-File
 */
@SuppressWarnings("CanBeFinal")
public class LithiumConfig {
    private static final Logger LOGGER = LogManager.getLogger("LithiumConfig");

    private Map<String, Option> mixinRules = new HashMap<>();

    private LithiumConfig() {
        this.addDefaultRule("ai.brain", true);
        this.addDefaultRule("ai.goal", true);
        this.addDefaultRule("ai.nearby_entity_tracking", true);
        this.addDefaultRule("ai.raid", true);
        this.addDefaultRule("avoid_allocations.enum_values", true);
        this.addDefaultRule("block.piston_shapes", true);
        this.addDefaultRule("cached_hashcode", true);
        this.addDefaultRule("chunk.no_locking", false);
        this.addDefaultRule("chunk.palette", true);
        this.addDefaultRule("chunk.serialization", true);
        this.addDefaultRule("client.fast_loading_screen", true);
        this.addDefaultRule("collections.entity_filtering", true);
        this.addDefaultRule("collections.sorted_array_set", true);
        this.addDefaultRule("entity.block_cache", true);
        this.addDefaultRule("entity.data_tracker", true);
        this.addDefaultRule("entity.simple_entity_block_collisions", true);
        this.addDefaultRule("entity.simple_world_border_collisions", true);
        this.addDefaultRule("entity.streamless_entity_retrieval", true);
        this.addDefaultRule("math.fast_util", true);
        this.addDefaultRule("poi.fast_init", true);
        this.addDefaultRule("poi.fast_retrieval", true);
        this.addDefaultRule("redstone", false);
        this.addDefaultRule("shapes.blockstate_cache", true);
        this.addDefaultRule("shapes.precompute_shape_arrays", true);
        this.addDefaultRule("shapes.shape_merging", true);
        this.addDefaultRule("shapes.specialized_shapes", true);
        this.addDefaultRule("world.chunk_task_system", true);
        this.addDefaultRule("world.chunk_tickets", true);
        this.addDefaultRule("world.chunk_ticking", true);
        this.addDefaultRule("world.explosions", true);
        this.addDefaultRule("world.tick_scheduler", true);

        this.mixinRules = Collections.unmodifiableMap(this.mixinRules);
    }

    /**
     * Adds a default rule entry which can later be modified by the user.
     */
    private void addDefaultRule(String mixin, boolean enabled) {
        this.mixinRules.put(mixin, new Option(enabled, false));
    }

    private void read(Toml toml) {
        Toml mixinConfig = toml.getTable("mixins");

        if (mixinConfig != null) {
            this.readRules(mixinConfig.getList("force_enabled"), true);
            this.readRules(mixinConfig.getList("force_disabled"), false);
        }
    }

    private void readRules(List<Object> rules, boolean enabled) {
        if (rules == null || rules.isEmpty()) {
            return;
        }

        for (Object obj : rules) {
            if (obj instanceof String) {
                this.addUserRule((String) obj, enabled);
            }
        }
    }

    /**
     * Updates an existing rule with a user-defined value.
     */
    private void addUserRule(String mixin, boolean enabled) {
        Option option = this.mixinRules.get(mixin);

        if (option != null) {
            option.setEnabled(enabled, true);
        } else {
            LOGGER.warn("No mixin rule exists with name '{}', skipping", mixin);
        }
    }

    /**
     * Returns the most specific Mixin rule for the specified class name.
     */
    public Option getEffectiveMixinRule(String mixinClassName) {
        int start = 0;
        int lastSplit = start;
        int nextSplit;

        Option rule = new Option(true, false);

        while ((nextSplit = mixinClassName.indexOf('.', lastSplit + 1)) != -1) {
            String part = mixinClassName.substring(start, nextSplit);

            Option candidate = this.mixinRules.get(part);

            if (candidate != null) {
                rule = candidate;
            }

            lastSplit = nextSplit;
        }

        return rule;
    }

    /**
     * Loads the configuration file from the specified location. If it does not exist, a new configuration file will be
     * created. The file on disk will then be updated to include any new options.
     */
    public static LithiumConfig load(File file) {
        if (!file.exists()) {
            writeDefaultConfig(file);

            return new LithiumConfig();
        }

        Toml toml = new Toml();
        toml.read(file);

        LithiumConfig config = new LithiumConfig();
        config.read(toml);

        return config;
    }

    private static void writeDefaultConfig(File file) {
        File dir = file.getParentFile();

        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("Could not create parent directories");
            }
        } else if (!dir.isDirectory()) {
            throw new RuntimeException("The parent file is not a directory");
        }

        try (Writer writer = new FileWriter(file)) {
            writer.write("# This is the configuration file for Lithium.\n");
            writer.write("#\n");
            writer.write("# You can find information on editing this file and all the available options here:\n");
            writer.write("# https://github.com/jellysquid3/lithium-fabric/wiki/Configuration-File\n");
            writer.write("#\n");
            writer.write("# By default, this file will be empty except for this notice.\n");
        } catch (IOException e) {
            throw new RuntimeException("Could not write default config", e);
        }
    }

    public int getRuleCount() {
        return this.mixinRules.size();
    }

    public int getRuleOverrideCount() {
        return (int) this.mixinRules.values()
                .stream()
                .filter(Option::isUserDefined)
                .count();
    }
}
