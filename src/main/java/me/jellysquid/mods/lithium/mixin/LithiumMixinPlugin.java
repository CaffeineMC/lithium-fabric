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
    private final HashSet<String> disabledPackages = new HashSet<>();

    @Override
    public void onLoad(String mixinPackage) {
        LithiumConfig config = LithiumConfig.load(new File("./config/lithium.json"));

        this.setupMixins(config);

        this.logger.info("Lithium's configuration file was loaded successfully");

        LithiumMod.CONFIG = config;
    }

    private void setupMixins(LithiumConfig config) {
        this.disableIf("avoid_allocations", !config.general.reduceObjectAllocations);
        this.disableIf("cached_hashcode", !config.general.cacheHashcodeCalculations);
        this.disableIf("chunk.fast_chunk_palette", !config.chunk.useOptimizedHashPalette);
        this.disableIf("client.avoid_cuboid_transformations", !config.client.reduceCuboidTransformations);
        this.disableIf("client.fast_loading_screen", !config.client.useLoadingScreenOptimizations);
        this.disableIf("client.replace_timer", !config.client.replaceClientTimeFunction);
        this.disableIf("entity.bitflag_goal_selection", !config.entity.useOptimizedAIGoalSelection);
        this.disableIf("entity.data_tracker.use_arrays", !config.entity.useOptimizedDataTracker);
        this.disableIf("entity.data_tracker.no_locks", !config.entity.avoidLockingDataTracker);
        this.disableIf("entity.fast_pathfind_chunk_cache", !config.entity.useOptimizedChunkCacheForPathFinding);
        this.disableIf("entity.simple_entity_block_collisions", !config.physics.useSimpleEntityCollisionTesting);
        this.disableIf("entity.simple_world_border_collisions", !config.physics.useFastWorldBorderChecks);
        this.disableIf("entity.skip_movement_tick", !config.entity.allowSkippingEntityMovementTicks);
        this.disableIf("fast_tick_scheduler", !config.general.useOptimizedTickScheduler);
        this.disableIf("no_debug_world_type", !config.chunk.disableDebugWorldType);
        this.disableIf("region.fast_session_lock", !config.region.reduceSessionLockChecks);
        this.disableIf("region.large_io", !config.region.useLargeIO);
        this.disableIf("region.mmap_files", !config.region.useMemoryMappedFileRegions);
        this.disableIf("small_tag_arrays", !config.other.useSmallTagArrayOptimization);
        this.disableIf("voxelshape.fast_shape_vertex_merging", !config.physics.useOptimizedShapeVertexListMerging);
        this.disableIf("voxelshape.precompute_shape_arrays", !config.physics.alwaysUnpackBlockShapes);
        this.disableIf("voxelshape.fast_shape_comparisons", !config.physics.useFastShapeComparisons);
        this.disableIf("chunk.no_chunk_locking", !config.chunk.removeConcurrentModificationChecks);
        this.disableIf("nbt.fast_serialization", !config.region.useFastNBTSerialization);
    }

    private void disableIf(String packageName, boolean condition) {
        if (condition) {
            this.disabledPackages.add(packageName);
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

            if (this.disabledPackages.contains(part)) {
                this.logger.info(String.format("Not applying mixin '%s' as the package '%s' is disabled by configuration", mixinClassName, part));

                return false;
            }

            lastSplit = nextSplit;
        }

        return true;
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
