package me.jellysquid.mods.lithium.mixin;

import me.jellysquid.mods.lithium.common.LithiumMod;
import me.jellysquid.mods.lithium.common.config.LithiumConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class LithiumMixinPlugin implements IMixinConfigPlugin {
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
        this.disableIf("chunk.fast_chunk_lock", !config.chunk.useTryLockInContainers);
        this.disableIf("chunk.fast_chunk_palette", !config.chunk.useOptimizedHashPalette);
        this.disableIf("chunk.pot_packed_array", !config.chunk.usePOTPackedIntegerArrays);
        this.disableIf("client.avoid_cuboid_transformations", !config.client.reduceCuboidTransformations);
        this.disableIf("client.fast_loading_screen", !config.client.useLoadingScreenOptimizations);
        this.disableIf("client.replace_timer", !config.client.replaceClientTimeFunction);
        this.disableIf("entity.bitflag_goal_selection", !config.entity.useOptimizedAIGoalSelection);
        this.disableIf("entity.fast_data_tracker", !config.entity.useOptimizedDataTracker);
        this.disableIf("entity.fast_pathfind_chunk_cache", !config.entity.useOptimizedChunkCacheForPathFinding);
        this.disableIf("entity.simple_entity_block_collisions", !config.physics.useSimpleEntityCollisionTesting);
        this.disableIf("entity.simple_world_border_collisions", !config.physics.useFastWorldBorderChecks);
        this.disableIf("entity.skip_movement_tick", !config.entity.allowSkippingEntityMovementTicks);
        this.disableIf("fast_tick_scheduler", !config.general.useOptimizedTickScheduler);
        this.disableIf("no_debug_world_type", !config.chunk.disableDebugWorldType);
        this.disableIf("region.buffered_reads", !config.region.useLargeReads);
        this.disableIf("small_tag_arrays", !config.other.useSmallTagArrayOptimization);
        this.disableIf("voxelshape.fast_shape_vertex_merging", !config.physics.useOptimizedShapeVertexListMerging);
        this.disableIf("voxelshape.precompute_shape_arrays", !config.physics.alwaysUnpackBlockShapes);
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
        if (!mixinClassName.startsWith("me.jellysquid.mods.lithium.mixin.")) {
            return true;
        }

        int s = "me.jellysquid.mods.lithium.mixin.".length();
        int i = s;
        int c;

        while ((c = mixinClassName.indexOf('.', i + 1)) != -1) {
            String part = mixinClassName.substring(s, c);

            if (this.disabledPackages.contains(part)) {
                this.logger.info(String.format("Not applying mixin '%s' as the package '%s' is disabled by configuration", mixinClassName, part));

                return false;
            }

            i = c;
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
