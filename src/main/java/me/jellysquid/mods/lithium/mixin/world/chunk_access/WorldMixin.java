package me.jellysquid.mods.lithium.mixin.world.chunk_access;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Implement the interface members of {@link LevelReader} and {@link CollisionGetter} directly to avoid complicated
 * method invocations between interface boundaries, helping the JVM to inline and optimize code.
 */
@Mixin(Level.class)
public abstract class WorldMixin implements LevelAccessor {
    /**
     * @reason Remove dynamic-dispatch and inline call
     * @author JellySquid
     */
    @Overwrite
    public LevelChunk getChunkAt(BlockPos pos) {
        return (LevelChunk) this.getChunk(pos);
    }

    @Override
    public ChunkAccess getChunk(BlockPos pos) {
        return this.getChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()), ChunkStatus.FULL, true);
    }

    /**
     * @reason Remove dynamic-dispatch and inline call
     * @author JellySquid
     */
    @Override
    @Overwrite
    public LevelChunk getChunk(int chunkX, int chunkZ) {
        return (LevelChunk) this.getChunk(chunkX, chunkZ, ChunkStatus.FULL, true);
    }

    @Override
    public ChunkAccess getChunk(int chunkX, int chunkZ, ChunkStatus status) {
        return this.getChunk(chunkX, chunkZ, status, true);
    }

    @Override
    public BlockGetter getChunkForCollisions(int chunkX, int chunkZ) {
        return this.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
    }
}
