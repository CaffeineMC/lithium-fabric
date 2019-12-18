# Changelog

### 0.2.0
- fix: Use the correct bounds check for chunk sections in the entity chunk cache
- fix: Ensure the entity chunk cache is purged on entity teleportation, fixes issues where the player would collide
with blocks in another dimension
- new: Use a simple AABB test to determine if an entity is colliding with a block if it's a normal, full cube. Provides
a decent speed up for the collision step for entities.
- new: Use a faster algorithm for compacting chunks during serialization (done when saving to disk)
- new: Use a flat array instead of a HashMap to store nearby chunks in the entity chunk cache and only populate
the range of chunks on demand
- new: Avoid the unnecessary Class#isAssignableFrom call in TypeFilterableList unless we are building a new cached list
- new: Replace World#isRegionLoaded calls made by entities to calls which go through the entity chunk cache
- new: Optimize VoxelShapes#method_20713 for a small improvement in light propagation code
- new: Added numerous patches to reduce the amount of CPU time needed to update region files through usage of memory-mapped
files
- new: Added experimental (and disabled by default) patches to disable concurrent modification checks in chunks
- new: Added experimental (again, disabled by default) patches for faster NBT serialization when writing to region files
- new: Avoid locking the DataTracker on every access and use a flat array for faster lookups
- new: Avoid constantly checking the world's session lock when saving chunks
- change: Removed the POT packed integer array patches as they don't improve performance

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