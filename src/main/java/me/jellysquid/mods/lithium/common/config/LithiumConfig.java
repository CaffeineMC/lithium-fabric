package me.jellysquid.mods.lithium.common.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * Documentation of these options: https://github.com/jellysquid3/lithium-fabric/wiki/Configuration-File
 */
@SuppressWarnings("CanBeFinal")
public class LithiumConfig {
    private static final Logger LOGGER = LogManager.getLogger("LithiumConfig");

    private final Map<String, Option> options = new HashMap<>();

    private LithiumConfig() {
        this.addDefaultMixinOption("ai.brain", true);
        this.addDefaultMixinOption("ai.goal", true);
        this.addDefaultMixinOption("ai.nearby_entity_tracking", true);
        this.addDefaultMixinOption("ai.raid", true);
        this.addDefaultMixinOption("avoid_allocations.enum_values", true);
        this.addDefaultMixinOption("block.piston_shapes", true);
        this.addDefaultMixinOption("cached_hashcode", true);
        this.addDefaultMixinOption("chunk.no_locking", false);
        this.addDefaultMixinOption("chunk.palette", true);
        this.addDefaultMixinOption("chunk.serialization", true);
        this.addDefaultMixinOption("client.fast_loading_screen", true);
        this.addDefaultMixinOption("collections.entity_filtering", true);
        this.addDefaultMixinOption("collections.sorted_array_set", true);
        this.addDefaultMixinOption("entity.block_cache", true);
        this.addDefaultMixinOption("entity.data_tracker", true);
        this.addDefaultMixinOption("entity.simple_entity_block_collisions", true);
        this.addDefaultMixinOption("entity.simple_world_border_collisions", true);
        this.addDefaultMixinOption("entity.streamless_entity_retrieval", true);
        this.addDefaultMixinOption("math.fast_util", true);
        this.addDefaultMixinOption("poi.fast_init", true);
        this.addDefaultMixinOption("poi.fast_retrieval", true);
        this.addDefaultMixinOption("redstone", false);
        this.addDefaultMixinOption("shapes.blockstate_cache", true);
        this.addDefaultMixinOption("shapes.precompute_shape_arrays", true);
        this.addDefaultMixinOption("shapes.shape_merging", true);
        this.addDefaultMixinOption("shapes.specialized_shapes", true);
        this.addDefaultMixinOption("world.chunk_task_system", true);
        this.addDefaultMixinOption("world.chunk_tickets", true);
        this.addDefaultMixinOption("world.chunk_ticking", true);
        this.addDefaultMixinOption("world.explosions", true);
        this.addDefaultMixinOption("world.tick_scheduler", true);
    }

    private void addDefaultMixinOption(String mixin, boolean enabled) {
        this.addDefaultOption(getMixinRuleName(mixin), enabled);
    }

    /**
     * Adds a default rule entry which can later be modified by the user.
     */
    private void addDefaultOption(String name, boolean enabled) {
        this.options.put(name, new Option(enabled, false));
    }

    private void read(Properties props) {
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

    /**
     * Returns the most specific Mixin rule for the specified class name.
     */
    public Option getOptionForMixin(String mixinClassName) {
        int start = 0;
        int lastSplit = start;
        int nextSplit;

        Option rule = new Option(true, false);

        while ((nextSplit = mixinClassName.indexOf('.', lastSplit + 1)) != -1) {
            String key = getMixinRuleName(mixinClassName.substring(start, nextSplit));

            Option candidate = this.options.get(key);

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
            try {
                writeDefaultConfig(file);
            } catch (IOException e) {
                LOGGER.warn("Could not write default configuration file", e);
            }

            return new LithiumConfig();
        }

        Properties props = new Properties();

        try (FileInputStream fin = new FileInputStream(file)){
            props.load(fin);
        } catch (IOException e) {
            throw new RuntimeException("Could not load config file", e);
        }

        LithiumConfig config = new LithiumConfig();
        config.read(props);

        return config;
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
                .filter(Option::isUserDefined)
                .count();
    }
}
