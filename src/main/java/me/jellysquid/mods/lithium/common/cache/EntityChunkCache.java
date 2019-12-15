package me.jellysquid.mods.lithium.common.cache;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

/**
 * Maintains a cached collection of chunks around an entity. This allows for much faster access to nearby chunks for
 * many entity related functions.
 */
public class EntityChunkCache extends AbstractCachedAccess {
    private final Long2ObjectMap<WorldChunk> chunks = new Long2ObjectOpenHashMap<>(9);

    @SuppressWarnings("unchecked")
    private final CachedEntry<WorldChunk>[] mra = new CachedEntry[3];

    private final Entity entity;

    private ChunkManager chunkManager;
    private World world;

    private int minX, minZ, maxX, maxZ;

    public EntityChunkCache(Entity entity) {
        this.entity = entity;
        this.world = entity.getEntityWorld();
        this.chunkManager = entity.getEntityWorld().getChunkManager();

        for (int i = 0; i < this.mra.length; i++) {
            this.mra[i] = new CachedEntry<>();
        }
    }

    public World getWorld() {
        return this.world;
    }

    public void updateChunks(Box box) {
        int minX = MathHelper.floor(box.minX - 16.0D) >> 4;
        int maxX = MathHelper.ceil(box.maxX + 16.0D) >> 4;
        int minZ = MathHelper.floor(box.minZ - 16.0D) >> 4;
        int maxZ = MathHelper.ceil(box.maxZ + 16.0D) >> 4;

        // If we're not watching any new chunks, we have no need to update the set.
        if (!this.chunks.isEmpty() && this.entity.getEntityWorld().getChunkManager() == this.chunkManager) {
            if (this.minX == minX && this.maxX == maxX && this.minZ == minZ && this.maxZ == maxZ) {
                return;
            }
        }

        this.chunks.clear();

        for (CachedEntry<WorldChunk> worldChunkCachedEntry : this.mra) {
            worldChunkCachedEntry.reset();
        }

        this.world = this.entity.getEntityWorld();
        this.chunkManager = this.world.getChunkManager();

        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                WorldChunk chunk = this.chunkManager.getWorldChunk(x, z, false);

                this.chunks.put(ChunkPos.toLong(x, z), chunk);
            }
        }

        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;
    }

    private ChunkSection getChunkSection(int x, int y, int z) {
        if (y < 0 || y >= 16) {
            return null;
        }

        Chunk chunk = this.getWorldChunk(x, z);

        if (chunk == null) {
            chunk = this.chunkManager.getWorldChunk(x, z, false);
        }

        if (chunk != null) {
            return chunk.getSectionArray()[y];
        }

        return null;
    }

    public BlockState getBlockState(BlockPos pos) {
        return this.getBlockState(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockState getBlockState(int x, int y, int z) {
        ChunkSection section = this.getChunkSection(x >> 4, y >> 4, z >> 4);

        if (section != null) {
            return section.getBlockState(x & 15, y & 15, z & 15);
        }

        return Blocks.AIR.getDefaultState();
    }

    public FluidState getFluidState(BlockPos pos) {
        return this.getFluidState(pos.getX(), pos.getY(), pos.getZ());
    }

    public FluidState getFluidState(int x, int y, int z) {
        ChunkSection section = this.getChunkSection(x >> 4, y >> 4, z >> 4);

        if (section != null) {
            return section.getFluidState(x & 15, y & 15, z & 15);
        }

        return Fluids.EMPTY.getDefaultState();
    }

    // We can avoid performing a lookup if the map is empty, which can sometimes happen.
    public WorldChunk getWorldChunk(int x, int z) {
        if (this.chunks.size() > 0) {
            long key = ChunkPos.toLong(x, z);

            for (CachedEntry<WorldChunk> worldChunkCachedEntry : this.mra) {
                if (worldChunkCachedEntry.pos == key) {
                    return worldChunkCachedEntry.obj;
                }
            }

            WorldChunk chunk = this.chunks.get(key);

            CachedEntry<WorldChunk> c2 = this.mra[2];
            c2.obj = chunk;
            c2.pos = key;

            this.mra[2] = this.mra[1];
            this.mra[1] = this.mra[0];
            this.mra[0] = c2;

            return chunk;
        } else {
            return null;
        }
    }
}
