package me.jellysquid.mods.lithium.mixin.world.chunk_access;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Implement the interface members of {@link WorldView} and {@link CollisionView} directly to avoid complicated
 * method invocations between interface boundaries, helping the JVM to inline and optimize code.
 */
@Mixin(World.class)
public abstract class WorldMixin implements WorldAccess {
    /**
     * @reason Remove dynamic-dispatch and inline call
     * @author JellySquid
     */
    @Overwrite
    public WorldChunk getWorldChunk(BlockPos pos) {
        return (WorldChunk) this.getChunk(pos);
    }

    @Override
    public Chunk getChunk(BlockPos pos) {
        return this.getChunkLithium(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, true);
    }

    /**
     * @reason Remove dynamic-dispatch and inline call
     * @author JellySquid
     */
    @Override
    @Overwrite
    public WorldChunk getChunk(int chunkX, int chunkZ) {
        return (WorldChunk) this.getChunkLithium(chunkX, chunkZ, ChunkStatus.FULL, true);
    }

    @Override
    public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus status) {
        return this.getChunkLithium(chunkX, chunkZ, status, true);
    }

    @Override
    public BlockView getChunkAsView(int chunkX, int chunkZ) {
        return this.getChunkLithium(chunkX, chunkZ, ChunkStatus.FULL, false);
    }

    private Chunk getChunkLithium(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) {
        Chunk chunk = this.getChunkManager().getChunk(chunkX, chunkZ, leastStatus, create);

        if (chunk == null && create) {
            throw new IllegalStateException("Should always be able to create a chunk!");
        } else {
            return chunk;
        }
    }
}
