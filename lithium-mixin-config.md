# Lithium Configuration File Summary
The configuration file makes use of the [Java properties format](https://docs.oracle.com/cd/E23095_01/Platform.93/ATGProgGuide/html/s0204propertiesfileformat01.html). If the configuration file does not exist during game start-up, a blank file with a comment will be created.

The configuration file defines *overrides* for the available options, and as such, a blank file is perfectly normal! It simply means that you'd like to use all the default values.

Each category below includes a list of options which can be changed by the user. Due to the nature of the mod, configuration options require a game restart to take effect.

### Editing the configuration file

Before editing the configuration file, take a backup of your minecraft worlds!
All configuration options are simple key-value pairs. In other words, you first specify the option's name, followed by the desired value, like so:

```properties
mixin.ai.pathing=false
mixin.gen.biome_noise_cache=false
```

# Configuration options

The options are available for each mod loader:

Fabric: [lithium-fabric-mixin-config.md](lithium-fabric-mixin-config.md)

NeoForge: [lithium-neoforge-mixin-config.md](lithium-neoforge-mixin-config.md)