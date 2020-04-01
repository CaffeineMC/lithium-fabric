package me.jellysquid.mods.lithium.common.config;

import me.jellysquid.mods.lithium.common.config.annotations.Category;
import me.jellysquid.mods.lithium.common.config.annotations.Option;
import me.jellysquid.mods.lithium.common.config.parser.ConfigParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Documentation of these options: https://github.com/jellysquid3/lithium-fabric/wiki/Configuration-File
 */
@SuppressWarnings("CanBeFinal")
public class LithiumConfig {
    @Category("ai")
    public static class AiConfig {
        @Option("use_fast_goal_selection")
        public boolean useFastGoalSelection = true;

        @Option("use_fast_raid_logic")
        public boolean useFastRaidLogic = true;

        @Option("use_nearby_entity_tracking")
        public boolean useNearbyEntityTracking = true;

        @Option("use_fast_brain")
        public boolean useFastBrain = true;
    }

    @Category("poi")
    public static class PoiConfig {
        @Option("use_fast_init")
        public boolean useFastInit = true;

        @Option("use_fast_retrieval")
        public boolean useFastRetrieval = true;
    }

    @Category("physics")
    public static class PhysicsConfig {
        @Option("use_fast_shape_comparison")
        public boolean useFastShapeComparisons = true;

        @Option("use_fast_vertex_merging")
        public boolean useFastVertexMerging = true;

        @Option("use_fast_world_border_checks")
        public boolean useFastWorldBorderChecks = true;

        @Option("use_simple_entity_collision_testing")
        public boolean useSimpleEntityCollisionTesting = true;

        @Option("always_unpack_block_shapes")
        public boolean alwaysUnpackBlockShapes = true;

        @Option("extend_block_shape_cache")
        public boolean extendBlockShapeCache = true;
    }

    @Category("client")
    public static class ClientConfig {
        @Option("use_loading_screen_optimizations")
        public boolean useLoadingScreenOptimizations = true;
    }

    @Category("entity")
    public static class EntityConfig {
        @Option("use_optimized_data_tracker")
        public boolean useOptimizedDataTracker = true;

        @Option("avoid_locking_data_tracker")
        public boolean avoidLockingDataTracker = true;

        @Option("use_streamless_entity_retrieval")
        public boolean useStreamlessEntityRetrieval = true;

        @Option("use_block_at_feet_caching")
        public boolean useBlockAtFeetCaching = true;
    }

    @Category("region")
    public static class RegionConfig {
        @Option("reduce_session_lock_checks")
        public boolean reduceSessionLockChecks = true;
    }

    @Category("chunk")
    public static class ChunkConfig {
        @Option("use_optimized_hash_palette")
        public boolean useOptimizedHashPalette = true;

        @Option("use_fast_palette_compaction")
        public boolean useFastPaletteCompaction = true;

        @Option("remove_concurrent_modification_checks")
        public boolean removeConcurrentModificationChecks = false;
    }

    @Category("general")
    public static class GeneralConfig {
        @Option("reduce_object_allocations")
        public boolean reduceObjectAllocations = true;

        @Option("cache_hashcode_calculations")
        public boolean cacheHashcodeCalculations = true;

        @Option("use_fast_math_utility_logic")
        public boolean useFastMathUtilityLogic = true;
    }

    @Category("world")
    public static class WorldConfig {
        @Option("use_fast_chunk_task_system")
        public boolean useFastChunkTaskSystem = true;

        @Option("use_fast_list_type_filtering")
        public boolean useFastListTypeFiltering = true;

        @Option("use_optimized_tick_scheduler")
        public boolean useOptimizedTickScheduler = true;

        @Option("use_fast_explosions")
        public boolean useFastExplosions = true;
    }

    @Category("redstone")
    public static class RedstoneConfig {
        @Option("use_redstone_dust_optimizations")
        public boolean useRedstoneDustOptimizations = false;
    }

    @Category("other")
    public static class OtherConfig {
        @Option("use_small_tag_array_optimization")
        public boolean useSmallTagArrayOptimization = true;
    }

    public AiConfig ai = new AiConfig();
    public PoiConfig poi = new PoiConfig();
    public GeneralConfig general = new GeneralConfig();
    public PhysicsConfig physics = new PhysicsConfig();
    public EntityConfig entity = new EntityConfig();
    public ChunkConfig chunk = new ChunkConfig();
    public ClientConfig client = new ClientConfig();
    public OtherConfig other = new OtherConfig();
    public RegionConfig region = new RegionConfig();
    public RedstoneConfig redstone = new RedstoneConfig();
    public WorldConfig world = new WorldConfig();

    /**
     * Loads the configuration file from the specified location. If it does not exist, a new configuration file will be
     * created. The file on disk will then be updated to include any new options.
     */
    public static LithiumConfig load(File file) {
        if (!file.exists()) {
            writeDefaultConfig(file);

            return new LithiumConfig();
        }

        try {
            return ConfigParser.deserialize(LithiumConfig.class, file);
        } catch (ConfigParser.ParseException e) {
            throw new RuntimeException("Could not parse config", e);
        }
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
}
