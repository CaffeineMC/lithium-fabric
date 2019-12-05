# Changelog

### 0.1.3
- fix: Mixins which target methods that only exist on the client side are loaded on the server, causing a crash
on startup
- change: Remove the locking optimization patch set for chunk data containers as it offers no benefit 
- change: Update to Loom 0.2.6 and use v2 mappings
- change: Include the Gradle wrapper in the repository
- doc: Include better documentation for some Mixins and use the reason attribute where applicable
- doc: Add code style and contribution guidelines

### 0.1.2
- fix: The game crashes when rendering special fonts (such as magical runes)

### 0.1.1
- fix: Chunks are sent over-the-wire in a format incompatible with the vanilla protocol, causing corrupted
terrain and crashes
- change: Removed usages of the shadow plugin and classes in the `net.minecraft` package
- change: Removed the `-mod` suffix from output JAR names
- change: Corrected the version format from `platform-version` to `version-platform`

### 0.1.0
Initial release.