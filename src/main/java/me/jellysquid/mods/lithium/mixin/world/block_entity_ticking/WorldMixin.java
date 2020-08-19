package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking;

import me.jellysquid.mods.lithium.common.util.collections.HashedList;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;

@Mixin(World.class)
public class WorldMixin {
    @Shadow
    @Mutable
    @Final
    private List<BlockEntity> unloadedBlockEntities;

    /**
     * Re-initializes the collection of unloaded block entities to a more specific optimized version,
     * which significantly improves server performance, since block entity unloading is a very frequent
     * process. It also has a good effect on the players' FPS.
     *
     * At the beginning of each tick, before all block entities are ticked, all those block entities that
     * are in the list of unloaded block entities are cleared from the main lists. The
     * {@link ArrayList#removeAll} methods are very time consuming, so if we use a hash table where
     * `contains`, `remove`, etc. have no O(n) problems (used in {@link ArrayList#removeAll}), we can
     * reduce server tick times of block entities and optimize the unloading process (MC-117075).
     *
     * TODO: I noticed that World#tickingBlockEntities copies the behavior of World#blockEntities. If one
     *  of these collections is deleted, extra performance can be achieved with a large number of
     *  block entities without breaking any vanilla behavior
     *
     * @author Maity
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void reinitialize(MutableWorldProperties properties, RegistryKey<World> registryKey,
                              final DimensionType dimensionType, Supplier<Profiler> profiler,
                              boolean isClient, boolean isDebugWorld, long seed, CallbackInfo ci) {
        this.unloadedBlockEntities = new HashedList<>(this.unloadedBlockEntities);
    }
}
