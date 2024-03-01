package me.jellysquid.mods.lithium.mixin.world.chunk_access;

import me.jellysquid.mods.lithium.common.world.chunk.ChunkHolderExtended;
import net.minecraft.class_9259;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReferenceArray;

@Mixin(ChunkHolder.class)
public class ChunkHolderMixin implements ChunkHolderExtended {
    @Shadow
    @Final
    private AtomicReferenceArray<CompletableFuture<class_9259<Chunk>>> futuresByStatus;

    private long lastRequestTime;

    @Override
    public CompletableFuture<class_9259<Chunk>> getFutureByStatus(int index) {
        return this.futuresByStatus.get(index);
    }

    @Override
    public void setFutureForStatus(int index, CompletableFuture<class_9259<Chunk>> future) {
        this.futuresByStatus.set(index, future);
    }

    @Override
    public boolean updateLastAccessTime(long time) {
        long prev = this.lastRequestTime;
        this.lastRequestTime = time;

        return prev != time;
    }
}
