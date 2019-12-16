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
         * If true, the code responsible for merging vertices when resolving collision models will be replaced with a faster
         * implementation which avoids bounds checking. This generally provides a significant boost and shouldn't cause
         * any issues.
         */
        public boolean useOptimizedShapeVertexListMerging = true;

        /**
         * If true, the temporary arrays which are used for vertex merging during collision detection will be cached and
         * stored in an object pool. This brings a huge reduction to the object allocation rate and significantly reduces
         * the garbage collector pressure caused by collision detection. However, this does have a very, very small impact
         * to the overall throughput of collision resolution.
         * <p>
         * In my testing, this trade-off is almost unmeasurable, and when it is measurable, the reduction to the amount of
         * time being spent in garbage collection often far outweighs it. Though, if you are using the Zero memory allocator
         * in Java 11+ with a very large heap (12GB+) that has plenty of free space for object allocations, it *might* be
         * faster to just let the garbage collector do its thing.
         * <p>
         * Summarily, you should never turn this off because it will almost always improve performance. In the cases where
         * it won't, you understand your software configuration very closely, have benchmarked the issue, and know exactly
         * what you're doing.
         */
        public boolean useAllocationPoolingForVertexListMerging = true;

        /**
         * If true, an array of values will be pre-calculated for every block shape. This will increase memory usage by a
         * small, but constant amount, while generally providing a performance boost when combined with the
         * useOptimizedCollisionVertexMerging option.
         */
        public boolean alwaysUnpackBlockShapes = true;

        /**
         * If true, the algorithm for checking whether or not an entity is within the world border will be replaced
         * with a much faster axis-aligned bounding box check. This will provide a benefit for all entities even if you
         * have not changed the default world border or are not near it.
         */
        public boolean useFastWorldBorderChecks = true;

        /**
         * If true, a simpler (and much faster) collision testing algorithm will be used to test if an entity is inside
         * blocks. This algorithm will fallback to the vanilla one if complex block shapes are encountered.
         */
        public boolean useSimpleEntityCollisionTesting = true;

        /**
         * If true, some comparisons involving voxel shapes will be optimized to avoid combining shapes unnecessarily.
         */
        public boolean useFastShapeComparisons = true;
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
         * If true, entity collision detection will not be performed if an entity is standing still or moving extremely
         * slowly (<0.0001 blocks/tick). This can have massive gains when many entities exist in a world, but could
         * (theoretically, at least) break mods which rely on this behavior.
         */
        public boolean allowSkippingEntityMovementTicks = true;

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
    }

    public static class RegionConfig {
        /**
         * If true, the number of I/O operations when reading/writing sector data from a region file will be reduced
         * to a single operation.
         */
        public boolean useLargeIO = true;

        /**
         * If true, memory-mapped file regions will be used to access and modify the chunk offset and timestamp tables
         * of a region file. This can provide a significant boost when opening new region files and when saving chunks.
         */
        public boolean useMemoryMappedFileRegions = true;

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
         * If true, checks which see if a chunk is being concurrently modified will be removed. This may slightly improve
         * performance in some situations, but comes with the dangerous side effect that these kinds of threading issues
         * will not be detected and could go on to create serious problems. This option **does not** make it valid to
         * access chunks from multiple threads.
         *
         * Disabled by default until further testing in vanilla is performed and alternatives are looked into.
         */
        public boolean removeConcurrentModificationChecks = false;
    }

    public static class GeneralConfig {
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

    public static class OtherConfig {

        /**
         * If true, tags with very few entries (<=5) will use array scanning instead of a hash table, which might slightly
         * improve performance when checking to see if a block/fluid is contained within a tag.
         */
        public boolean useSmallTagArrayOptimization = true;
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
