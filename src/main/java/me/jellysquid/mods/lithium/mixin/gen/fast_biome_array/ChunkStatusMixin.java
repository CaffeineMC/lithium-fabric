package me.jellysquid.mods.lithium.mixin.gen.fast_biome_array;

import java.util.List;

import me.jellysquid.mods.lithium.common.world.biome.HorizontalBiomeArray;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.source.HorizontalVoronoiBiomeAccessType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;

@Mixin(ChunkStatus.class)
public class ChunkStatusMixin {
    @Inject(method = "method_16570", at = @At("HEAD"), remap = false, cancellable = true)
    private static void populateBiomes(ServerWorld world, ChunkGenerator generator, List<Chunk> surroundingChunks, Chunk chunk, CallbackInfo info) {
        if (world.getDimension().getBiomeAccessType() instanceof HorizontalVoronoiBiomeAccessType) {
            ((ProtoChunk) chunk).setBiomes(new HorizontalBiomeArray(world.getRegistryManager().get(Registry.BIOME_KEY), chunk.getPos(), generator.getBiomeSource()));

            info.cancel();
        }
    }
}
