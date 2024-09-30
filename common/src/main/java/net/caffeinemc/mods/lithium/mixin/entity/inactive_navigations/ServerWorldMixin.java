package net.caffeinemc.mods.lithium.mixin.entity.inactive_navigations;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.caffeinemc.mods.lithium.common.entity.NavigatingEntity;
import net.caffeinemc.mods.lithium.common.world.LithiumData;
import net.caffeinemc.mods.lithium.common.world.ServerWorldExtended;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * This patch is supposed to reduce the cost of setblockstate calls that change the collision shape of a block.
 * In vanilla, changing the collision shape of a block will notify *ALL* MobEntities in the world.
 * Instead, we track which EntityNavigation is going to be used by a MobEntity and
 * call the update code on the navigation directly.
 * As EntityNavigations only care about these changes when they actually have a currentPath, we skip the iteration
 * of many EntityNavigations. For that optimization we need to track whether navigations have a path.
 * <p>
 * Another possible optimization for the future: By using the entity section registration tracking of 1.17,
 * we can partition the active navigation set by region/chunk/etc. to be able to iterate over nearby EntityNavigations.
 * In vanilla the limit calculation includes the path length entity position, which can change very often and force us
 * to update the registration very often, which could cost a lot of performance.
 * As the number of block changes is generally way higher than the number of mobs pathfinding, the update code would
 * need to be triggered by the mobs pathfinding.
 */
@Mixin(ServerLevel.class)
public abstract class ServerWorldMixin extends Level implements ServerWorldExtended {
    @Mutable
    @Shadow
    @Final
    Set<Mob> navigatingMobs;

    protected ServerWorldMixin(WritableLevelData properties, ResourceKey<Level> registryRef, RegistryAccess registryManager, Holder<DimensionType> dimensionEntry, Supplier<ProfilerFiller> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }


    /**
     * Optimization: Only update listeners that may care about the update. Listeners which have no path
     * never react to the update.
     * With thousands of non-pathfinding mobs in the world, this can be a relevant difference.
     */
    @Redirect(
            method = "sendBlockUpdated(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Set;iterator()Ljava/util/Iterator;"
            )
    )
    private Iterator<Mob> getActiveListeners(Set<Mob> set) {
        return Collections.emptyIterator();
    }

    @SuppressWarnings("rawtypes")
    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(MinecraftServer server, Executor workerExecutor, LevelStorageSource.LevelStorageAccess session, ServerLevelData properties, ResourceKey worldKey, LevelStem dimensionOptions, ChunkProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List spawners, boolean shouldTickTime, RandomSequences randomSequencesState, CallbackInfo ci) {
        this.navigatingMobs = new ReferenceOpenHashSet<>(this.navigatingMobs);
    }

    @Override
    public void lithium$setNavigationActive(Mob mobEntity) {
        ReferenceOpenHashSet<PathNavigation> activeNavigations = ((LithiumData) this).lithium$getData().activeNavigations();
        activeNavigations.add(((NavigatingEntity) mobEntity).lithium$getRegisteredNavigation());
    }

    @Override
    public void lithium$setNavigationInactive(Mob mobEntity) {
        ReferenceOpenHashSet<PathNavigation> activeNavigations = ((LithiumData) this).lithium$getData().activeNavigations();
        activeNavigations.remove(((NavigatingEntity) mobEntity).lithium$getRegisteredNavigation());
    }

    @Inject(
            method = "sendBlockUpdated(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Set;iterator()Ljava/util/Iterator;"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void updateActiveListeners(BlockPos pos, BlockState oldState, BlockState newState, int arg3, CallbackInfo ci, VoxelShape string, VoxelShape voxelShape, List<PathNavigation> list) {
        ReferenceOpenHashSet<PathNavigation> activeNavigations = ((LithiumData) this).lithium$getData().activeNavigations();
        for (PathNavigation entityNavigation : activeNavigations) {
            if (entityNavigation.shouldRecomputePath(pos)) {
                list.add(entityNavigation);
            }
        }
    }

    /**
     * Debug function
     *
     * @return whether the activeEntityNavigation set is in the correct state
     */
    @Unique
    @SuppressWarnings("unused")
    public boolean areEntityNavigationsConsistent() {
        ReferenceOpenHashSet<PathNavigation> activeNavigations = ((LithiumData) this).lithium$getData().activeNavigations();
        int i = 0;
        for (Mob mobEntity : this.navigatingMobs) {
            PathNavigation entityNavigation = mobEntity.getNavigation();
            if ((entityNavigation.getPath() != null && ((NavigatingEntity) mobEntity).lithium$isRegisteredToWorld()) != activeNavigations.contains(entityNavigation)) {
                return false;
            }
            if (entityNavigation.getPath() != null) {
                i++;
            }
        }
        return activeNavigations.size() == i;
    }
}
