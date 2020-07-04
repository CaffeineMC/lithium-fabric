package me.jellysquid.mods.lithium.common.config;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Documentation of these options: https://github.com/jellysquid3/lithium-fabric/wiki/Configuration-File
 */
@SuppressWarnings("CanBeFinal")
public class LithiumConfig {
    private static final Logger LOGGER = LogManager.getLogger("LithiumConfig");

    private final Map<String, Option> options = new HashMap<>();

    private LithiumConfig() {
        this.addDefaultMixinOption("chunk.no_locking", false);
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

    private void discoverMixins(String path) {
        try (InputStream in = LithiumConfig.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IOException("Could not find mixin config at path: " + path);
            }

            JsonObject mixinConfig = new Gson().fromJson(new InputStreamReader(in), JsonObject.class);

            this.addKnownMixins(mixinConfig.getAsJsonArray("mixins"));
            this.addKnownMixins(mixinConfig.getAsJsonArray("client"));
        } catch (IOException e) {
            throw new RuntimeException("Could not examine mixin config", e);
        }
    }

    private void addKnownMixins(JsonArray array) {
        // Assume it's empty
        if (array == null) {
            return;
        }

        for (JsonElement e : array) {
            String name = e.getAsString();
            String packageName = name.substring(0, name.lastIndexOf('.'));

            this.addKnownMixinName(packageName);
        }
    }

    private void addKnownMixinName(String name) {
        this.options.computeIfAbsent(getMixinRuleName(name), (key) -> new Option(true, false));
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
    public static LithiumConfig load(File file, String mixinPath) {
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
        config.discoverMixins(mixinPath);
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
