package me.jellysquid.mods.lithium.mixin.entity.slimechunk_cache;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.mojang.datafixers.util.Either;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.jellysquid.mods.lithium.common.chunk.SlimeChunkStorage;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

@Mixin(ChunkStatus.class)
public class ChunkStatusMixin {
    @Inject(at = @At("RETURN"), method = "runGenerationTask")
    public void genSlimeChunks(ServerWorld world, ChunkGenerator chunkGenerator, StructureManager structureManager, ServerLightingProvider lightingProvider, Function<Chunk, CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> function, List<Chunk> chunks, CallbackInfoReturnable<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> cir) {
        if(world.getDimension() == DimensionType.getOverworldDimensionType()) {

            for(Chunk currentChunk : chunks) {
                ChunkPos chunkPos = currentChunk.getPos();
                boolean isSlimeChunk = ChunkRandom.getSlimeRandom(chunkPos.x, chunkPos.z, ((ServerWorldAccess)world).getSeed(), 987234911L).nextInt(10) == 0;

                if(isSlimeChunk) {
                    SlimeChunkStorage.addSlimeChunk(chunkPos);
                }
            }
        }
    }
}