package me.jellysquid.mods.lithium.mixin.redstone;

import me.jellysquid.mods.lithium.common.block.redstone.RedstoneEngine;
import me.jellysquid.mods.lithium.common.block.redstone.WorldWithRedstoneEngine;
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
 * Installs the {@link RedstoneEngine} for a world.
 */
@Mixin(World.class)
public class MixinWorld implements WorldWithRedstoneEngine {
    private RedstoneEngine redstoneEngine;

    /**
     * Initialize the redstone engine and store it in the world object.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(LevelProperties levelProperties, DimensionType dimensionType, BiFunction<World, Dimension, ChunkManager> chunkManagerProvider, Supplier<Profiler> profiler, boolean isClient, CallbackInfo ci) {
        this.redstoneEngine = new RedstoneEngine((World) (Object) this);
    }

    @Override
    public RedstoneEngine getRedstoneEngine() {
        return this.redstoneEngine;
    }
}
