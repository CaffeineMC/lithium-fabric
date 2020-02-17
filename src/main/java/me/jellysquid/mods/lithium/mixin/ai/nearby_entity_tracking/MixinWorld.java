package me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking;

import me.jellysquid.mods.lithium.common.entity.tracker.EntityTrackerEngine;
import me.jellysquid.mods.lithium.common.entity.tracker.WorldWithEntityTrackerEngine;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Extends the base world class to provide a {@link EntityTrackerEngine}.
 */
@Mixin(World.class)
public class MixinWorld implements WorldWithEntityTrackerEngine {
    private EntityTrackerEngine tracker;

    /**
     * Initialize the {@link EntityTrackerEngine} which all entities of the world will interact with.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(LevelProperties levelProperties, DimensionType dimensionType, BiFunction<World, Dimension, ChunkManager> chunkManagerProvider, Supplier<Profiler> profiler, boolean isClient, CallbackInfo ci) {
        this.tracker = new EntityTrackerEngine();
    }

    @Override
    public EntityTrackerEngine getEntityTracker() {
        return this.tracker;
    }
}
