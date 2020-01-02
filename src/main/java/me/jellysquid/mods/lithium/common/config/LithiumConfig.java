package me.jellysquid.mods.lithium.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@SuppressWarnings("CanBeFinal")
public class LithiumConfig {
    public static class PhysicsConfig {
        /**
         * If true, a more precise algorithm will be used for determining which blocks an entity is going to intersect
         * each tick. This can significantly reduce the number of blocks considered for collision resolution, which will
         * in turn provide a performance boost for each entity. In situations where entities are trying to move very
         * quickly (such as in a mob farm where an entity is being pushed by many others), this algorithm will reduce
         * the number of blocks being collision tested by multiple orders of magnitudes and greatly improve performance.
         */
        public boolean useSweptCollisionSearch = true;

        /**
         * If true, shape comparision will use optimized algorithms when simple cube shapes are involved. This allows
         * for much faster collision resolution against shapes which only contain one cube and will provide a large
         * boost to performance.
         */
        public boolean useFastShapeComparisons = true;

        /**
         * If true, the algorithm for checking whether or not an entity is within the world border will be replaced
         * with a much faster axis-aligned bounding box check. This will provide a benefit for all entities even if you
         * have not changed the default world border or are not near it.
         */
        public boolean useFastWorldBorderChecks = true;

        /**
         * If true, a simpler (and much faster) collision testing algorithm will be used to test if an entity is inside
         * blocks. Additionally, the world border will not be included in a collision test if the player will not collide
         * with it in a physics step.
         */
        public boolean useSimpleEntityCollisionTesting = true;

        /**
         * If true, an array of values will be pre-calculated for every block shape. This will increase memory usage by a
         * small, but constant amount, while generally providing a performance boost when combined with the
         * useOptimizedCollisionVertexMerging option.
         */
        public boolean alwaysUnpackBlockShapes = true;
    }

    public static class ClientConfig {
        /**
         * If true, the client's time function (glfwGetTime()) will be replaced with standard Java calls. This can improve
         * performance when multiple threads (i.e. the client and server) are both accessing the timer, which is usually
         * always the case in single-player.
         */
        public boolean replaceClientTimeFunction = true;

        /**
         * If true, matrix transformations performed during entity rendering will be reduced as much as possible. This
         * generally is a safe option to enable and improves performance significantly for complex models with lots of parenting.
         */
        public boolean reduceCuboidTransformations = true;

        /**
         * If true, a number of optimizations will be applied to the loading screen to reduce CPU usage while it is being
         * drawn by batching all progress square renders into a single draw call.
         */
        public boolean useLoadingScreenOptimizations = true;
    }

    public static class EntityConfig {
        /**
         * If true, an optimized map implementation will be used for entity data tracking which avoids integer boxing
         * and a map lookup by using a simple array.
         */
        public boolean useOptimizedDataTracker = true;

        /**
         * If true, the data tracker for entities will not perform locking. This works by making additional patches
         * to some network packet classes, requiring them to copy their data on the main-thread which will be serialized
         * later off-thread. This could (however unlikely) cause issues if mods are installed which try to access the
         * data tracker on the wrong thread.
         */
        public boolean avoidLockingDataTracker = false;

        /**
         * If true, a faster implementation will be used for selecting AI goals for entities. The original code was
         * heavily obfuscated and it is uncertain whether or not this behaves identically to vanilla, but it seems to work
         * perfectly fine in limited testing.
         */
        public boolean useOptimizedAIGoalSelection = true;

        /**
         * If true, entities will cache nearby chunks to avoid more expensive calls to the world object.
         */
        public boolean useChunkCacheForEntities = true;

        /**
         * If true, an optimized implementation of the chunk cache will be used when entities are path-finding.
         */
        public boolean useOptimizedChunkCacheForPathFinding = true;

        /**
         * If true, entities will be selected for collision using an optimized function which avoids functional
         * stream-heavy code. This will generally provide a boost when entities are heavily crowded.
         */
        public boolean useStreamlessEntityRetrieval = true;
    }

    public static class RegionConfig {
        /**
         * If true, the world's session lock will only be checked once before saving all pending chunks versus once
         * for every chunk saved.
         */
        public boolean reduceSessionLockChecks = true;
    }

    public static class ChunkConfig {
        /**
         * If true, extra conditional logic for checking if a world is of the debug type will be removed. This may slightly
         * improve performance in some situations, but will make it impossible to use the debug world type. It's very likely
         * you didn't know that was a feature or will ever need to use it, so this option is generally worthwhile.
         */
        public boolean disableDebugWorldType = true;

        /**
         * If true, the vanilla hash palette which maps a small range of integers in chunk data into blocks will be replaced
         * with a version that has better performance when placing blocks into chunks.
         */
        public boolean useOptimizedHashPalette = true;

        /**
         * If true, an optimized method for compacting chunk data arrays and palettes upon serialization will be used.
         * This is greatly faster than the vanilla implementation and should be safe to use.
         */
        public boolean useFastCompaction = false;

        /**
         * If true, checks which see if a chunk is being concurrently modified will be removed. This may slightly improve
         * performance in some situations, but comes with the dangerous side effect that these kinds of threading issues
         * will not be detected and could go on to create serious problems. This option **does not** make it valid to
         * access chunks from multiple threads.
         * <p>
         * Disabled by default until further testing in vanilla is performed and alternatives are looked into.
         */
        public boolean removeConcurrentModificationChecks = false;
    }

    public static class GeneralConfig {
        /**
         * If true, the expensive check to see if a TypeFilterableList can be filtered by a specific class will only be
         * made when a new list for that type needs to be created.
         */
        public boolean useFastListTypeFiltering = true;

        /**
         * If true, the tick scheduler will be replaced with an optimized variant which allows for significantly reduced CPU
         * usage with many scheduled ticks and much faster checking of in-progress ticks. In real world terms, this means
         * that the tick settling which occurs right after generating chunks will take much less time, and redstone ticking
         * will be slightly faster. This implementation has been tested extensively, but some issues might still lurk.
         */
        public boolean useOptimizedTickScheduler = true;

        /**
         * If true, a handful of small patches will be made to avoid unnecessary object allocation throughout the game.
         */
        public boolean reduceObjectAllocations = true;

        /**
         * If true, a handful of small patches will be made to avoid unnecessary hashcode recalculation throughout the game.
         */
        public boolean cacheHashcodeCalculations = true;
    }

    public static class RedstoneConfig {
        /**
         * If true, Redstone dust will use an optimized update system which avoids unnecessary block updates (fixing MC-81098).
         * Additionally, the update order of redstone dust is made deterministic (fixing MC-11193).
         *
         * These patches will provide a huge improvement when updating dust, but might affect contraptions which rely on the
         * non-deterministic update order of dust (known as "locational contraptions"). This is unfortunate and goes
         * against the strongest belief of the mod (being that we don't change vanilla behaviours) but it is impossible
         * to maintain behaviour which is non-deterministic. Even though this behaviour is often said to be consistent
         * given the same location in a world, this is only a happy coincidence. The implementation of Set (which is the
         * type responsible for ordering these updates) makes no guarantees about its order, meaning that a simple update
         * to Java or the usage of another JVM could result in the order being changed.
         *
         * This is disabled by default as it is an INCUBATING feature. This will likely conflict with any other mods
         * which make similar patches. Please report issues with this option enabled.
         */
        public boolean useRedstoneDustOptimizations = false;
    }

    public static class OtherConfig {

        /**
         * If true, tags with very few entries (<=5) will use array scanning instead of a hash table, which might slightly
         * improve performance when checking to see if a block/fluid is contained within a tag.
         */
        public boolean useSmallTagArrayOptimization = true;
    }

    public static class DebugConfig {
        /**
         * If true, the client will be patched to allow the visualization of "tracers" used for debugging.
         */
        public boolean allowTracerVisualization = false;

        /**
         * If true, swept entity bounding box collisions will be traced. This will add non-trivial overhead!
         */
        public boolean traceSweptCollisions = false;
    }

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public GeneralConfig general = new GeneralConfig();
    public PhysicsConfig physics = new PhysicsConfig();
    public EntityConfig entity = new EntityConfig();
    public ChunkConfig chunk = new ChunkConfig();
    public ClientConfig client = new ClientConfig();
    public OtherConfig other = new OtherConfig();
    public RegionConfig region = new RegionConfig();
    public RedstoneConfig redstone = new RedstoneConfig();
    public DebugConfig debug = new DebugConfig();

    /**
     * Loads the configuration file from the specified location. If it does not exist, a new configuration file will be
     * created. The file on disk will then be updated to include any new options.
     */
    public static LithiumConfig load(File file) {
        LithiumConfig config;

        if (!file.exists()) {
            config = new LithiumConfig();
        } else {
            try (FileReader in = new FileReader(file)) {
                config = gson.fromJson(in, LithiumConfig.class);
            } catch (IOException e) {
                throw new RuntimeException("Couldn't load config", e);
            }
        }

        config.save(file);

        return config;
    }

    /**
     * Saves the configuration file to disk, creating any directories as needed.
     */
    private void save(File file) {
        File parent = file.getParentFile();

        if (!parent.exists() && !parent.mkdirs()) {
            throw new RuntimeException("Couldn't create config directory");
        }

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't save config");
        }
    }
}
