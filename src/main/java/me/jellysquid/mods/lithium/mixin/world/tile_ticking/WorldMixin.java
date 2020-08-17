package me.jellysquid.mods.lithium.mixin.world.tile_ticking;

import me.jellysquid.mods.lithium.common.util.collections.HashSetBackedList;
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

import java.util.List;
import java.util.function.Supplier;

@Mixin(World.class)
public class WorldMixin {
    @Shadow
    @Mutable
    @Final
    private List<BlockEntity> unloadedBlockEntities;

    /**
     * Re-initializing the collection of block entities that should be unloaded if it is not empty at
     * the beginning of each tick to a more specifically optimized variant {@link java.util.HashSet},
     * since it has no O(n) problems for methods like `remove`, `contains`, etc. used to remove unloaded
     * block entities from the main collection of block entities that should be ticked
     * ({@link World#blockEntities} or World#tickingBlockEntities.
     *
     * This will greatly increase FPS and server tick times when unloading a large number of
     * block entities, which happens very often.
     *
     * TODO I noticed that World#tickingBlockEntities copies the behavior of World#blockEntities. If one
     *  of these collections is deleted, extra performance can be achieved with a large number of
     *  block entities without breaking any vanilla behavior
     *
     * @author Maity
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void reinitialize(MutableWorldProperties properties, RegistryKey<World> registryKey,
                              final DimensionType dimensionType, Supplier<Profiler> supplier,
                              boolean isClient, boolean debugWorld, long seed, CallbackInfo ci) {
        this.unloadedBlockEntities = new HashSetBackedList<>();
    }
}
