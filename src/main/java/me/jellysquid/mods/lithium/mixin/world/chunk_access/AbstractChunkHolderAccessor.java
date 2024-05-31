package me.jellysquid.mods.lithium.mixin.world.chunk_access;

import net.minecraft.server.world.OptionalChunk;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReferenceArray;

@Mixin(AbstractChunkHolder.class)
public interface AbstractChunkHolderAccessor {

    @Accessor("chunkFuturesByStatus")
    AtomicReferenceArray<CompletableFuture<OptionalChunk<Chunk>>> lithium$getChunkFuturesByStatus();

    @Invoker("cannotBeLoaded")
    boolean invokeCannotBeLoaded(ChunkStatus status);
}
