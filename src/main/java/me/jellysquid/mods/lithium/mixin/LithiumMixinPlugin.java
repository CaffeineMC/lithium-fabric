package me.jellysquid.mods.lithium.mixin;

import me.jellysquid.mods.lithium.common.LithiumMod;
import me.jellysquid.mods.lithium.common.config.LithiumConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class LithiumMixinPlugin implements IMixinConfigPlugin {
    private static final String MIXIN_PACKAGE_ROOT = "me.jellysquid.mods.lithium.mixin.";

    private final Logger logger = LogManager.getLogger("Lithium");
    private final HashSet<String> enabledPackages = new HashSet<>();

    @Override
    public void onLoad(String mixinPackage) {
        LithiumConfig config = LithiumConfig.load(new File("./config/lithium.toml"));

        this.setupMixins(config);

        this.logger.info("Lithium's configuration file was loaded successfully");

        LithiumMod.CONFIG = config;
    }

    private void setupMixins(LithiumConfig config) {
        this.enableIf("ai.fast_brain", config.ai.useFastBrain);
        this.enableIf("ai.fast_goal_selection", config.ai.useFastGoalSelection);
        this.enableIf("ai.fast_raids", config.ai.useFastRaidLogic);
        this.enableIf("ai.nearby_entity_tracking", config.ai.useNearbyEntityTracking);
        this.enableIf("avoid_allocations", config.general.reduceObjectAllocations);
        this.enableIf("cached_hashcode", config.general.cacheHashcodeCalculations);
        this.enableIf("chunk.fast_chunk_palette", config.chunk.useOptimizedHashPalette);
        this.enableIf("chunk.fast_chunk_serialization", config.chunk.useFastPaletteCompaction);
        this.enableIf("chunk.no_chunk_locking", config.chunk.removeConcurrentModificationChecks);
        this.enableIf("client.fast_loading_screen", config.client.useLoadingScreenOptimizations);
        this.enableIf("entity.block_cache", config.entity.useBlockAtFeetCaching);
        this.enableIf("entity.data_tracker.no_locks", config.entity.avoidLockingDataTracker);
        this.enableIf("entity.data_tracker.use_arrays", config.entity.useOptimizedDataTracker);
        this.enableIf("entity.simple_entity_block_collisions", config.physics.useSimpleEntityCollisionTesting);
        this.enableIf("entity.simple_world_border_collisions", config.physics.useFastWorldBorderChecks);
        this.enableIf("entity.streamless_entity_retrieval", config.entity.useStreamlessEntityRetrieval);
        this.enableIf("math.fast_util", config.general.useFastMathUtilityLogic);
        this.enableIf("redstone", config.redstone.useRedstoneDustOptimizations);
        this.enableIf("region.fast_session_lock", config.region.reduceSessionLockChecks);
        this.enableIf("small_tag_arrays", config.other.useSmallTagArrayOptimization);
        this.enableIf("voxelshape.block_shape_cache", config.physics.extendBlockShapeCache);
        this.enableIf("voxelshape.fast_shape_comparisons", config.physics.useFastShapeComparisons);
        this.enableIf("voxelshape.precompute_shape_arrays", config.physics.alwaysUnpackBlockShapes);
        this.enableIf("voxelshape.fast_vertex_merging", config.physics.useFastVertexMerging);
        this.enableIf("world.fast_tick_scheduler", config.world.useOptimizedTickScheduler);
        this.enableIf("world.fast_type_filterable_list", config.world.useFastListTypeFiltering);
        this.enableIf("world.fast_explosions", config.world.useFastExplosions);
        this.enableIf("world.fast_chunk_task_system", config.world.useFastChunkTaskSystem);
        this.enableIf("poi.fast_retrieval", config.poi.useFastRetrieval);
        this.enableIf("poi.fast_init", config.poi.useFastInit);
    }

    private void enableIf(String packageName, boolean condition) {
        if (condition) {
            this.enabledPackages.add(packageName);
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!mixinClassName.startsWith(MIXIN_PACKAGE_ROOT)) {
            return true;
        }

        int start = MIXIN_PACKAGE_ROOT.length();
        int lastSplit = start;
        int nextSplit;

        while ((nextSplit = mixinClassName.indexOf('.', lastSplit + 1)) != -1) {
            String part = mixinClassName.substring(start, nextSplit);

            if (this.enabledPackages.contains(part)) {
                return true;
            }

            lastSplit = nextSplit;
        }

        this.logger.info("Not applying mixin '" + mixinClassName + "' as no configuration enables it");

        return false;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
