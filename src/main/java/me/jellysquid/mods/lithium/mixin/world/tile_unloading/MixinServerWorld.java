package me.jellysquid.mods.lithium.mixin.world.tile_unloading;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld extends World {
    private MixinServerWorld(MutableWorldProperties props, RegistryKey<World> worldKey, RegistryKey<DimensionType> dimensionKey, DimensionType type, Supplier<Profiler> profiler, boolean isDebugWorld, boolean isClient, long seed) {
        super(props, worldKey, dimensionKey, type, profiler, isDebugWorld, isClient, seed);
    }

    @Redirect(method = "dump", at = @At(value = "INVOKE",
            target = "Ljava/util/List;size()I"))
    private int changeCollectionSizeLink(List<BlockEntity> blockEntities) {
        return this.tickingBlockEntities.size();
    }

    @Redirect(method = "dumpBlockEntities", at = @At(value = "INVOKE",
            target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    private Iterator<BlockEntity> changeCollectionIteratorLink(List<BlockEntity> blockEntities) {
        return this.tickingBlockEntities.iterator();
    }
}
