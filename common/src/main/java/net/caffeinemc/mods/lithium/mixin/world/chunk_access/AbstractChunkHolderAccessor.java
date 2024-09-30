package net.caffeinemc.mods.lithium.mixin.world.chunk_access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReferenceArray;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;

@Mixin(GenerationChunkHolder.class)
public interface AbstractChunkHolderAccessor {

    @Accessor("futures")
    AtomicReferenceArray<CompletableFuture<ChunkResult<ChunkAccess>>> lithium$getChunkFuturesByStatus();

    @Invoker("isStatusDisallowed")
    boolean invokeCannotBeLoaded(ChunkStatus status);
}
