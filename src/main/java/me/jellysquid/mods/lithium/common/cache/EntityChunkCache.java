package me.jellysquid.mods.lithium.common.cache;

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

import java.util.Arrays;

import static net.minecraft.util.math.ChunkSectionPos.getLocalCoord;
import static net.minecraft.util.math.ChunkSectionPos.getSectionCoord;

/**
 * Maintains a cached collection of chunks around an entity. This allows for much faster access to nearby chunks for
 * many entity related functions.
 */
public class EntityChunkCache {
    private static final int RADIUS = 1;

    private static final int LENGTH = (RADIUS * 2) + 1;

    private final Entity entity;

    private WorldChunk[] chunks = new WorldChunk[LENGTH * LENGTH];

    private ChunkManager chunkManager;

    private int startX, startZ;

    private boolean isCacheReusable = false;

    private WorldChunk prev;
    private long prevPos;

    public EntityChunkCache(Entity entity) {
        this.entity = entity;
        this.chunkManager = entity.getEntityWorld().getChunkManager();

        this.prev = null;
        this.prevPos = Long.MIN_VALUE;
    }

    public World getWorld() {
        return this.entity.getEntityWorld();
    }

    public void updateChunks(Box box) {
        int startX = getSectionCoord(MathHelper.floor(box.x1)) - RADIUS;
        int startZ = getSectionCoord(MathHelper.floor(box.z1)) - RADIUS;

        ChunkManager chunkManager = this.getWorld().getChunkManager();

        if (chunkManager != this.chunkManager) {
            this.chunkManager = chunkManager;

            this.isCacheReusable = false;
        }

        // If the entity's world changes (such as due to a teleportation), wipe the cache
        if (this.isCacheReusable) {
            if (startX == this.startX && startZ == this.startZ) {
                this.updateChunksInPlace(startX, startZ);
                return;
            }
        } else {
            Arrays.fill(this.chunks, null);

            this.prev = null;
            this.prevPos = Long.MIN_VALUE;

            this.isCacheReusable = true;
        }

        WorldChunk[] chunkCache = new WorldChunk[LENGTH * LENGTH];

        for (int x = 0; x < LENGTH; x++) {
            for (int z = 0; z < LENGTH; z++) {
                chunkCache[index2(x, z)] = this.chunkManager.getWorldChunk(startX + x, startZ + z);
            }
        }

        this.chunks = chunkCache;

        this.startX = startX;
        this.startZ = startZ;
    }

    private void updateChunksInPlace(int startX, int startZ) {
        for (int x = 0; x < LENGTH; x++) {
            for (int z = 0; z < LENGTH; z++) {
                this.chunks[index2(x, z)] = this.fetchChunk(startX + x, startZ + z);
            }
        }
    }

    public BlockState getBlockState(BlockPos pos) {
        return this.getBlockState(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockState getBlockState(int x, int y, int z) {
        ChunkSection section = this.getChunkSection(getSectionCoord(x), getSectionCoord(y), getSectionCoord(z));

        if (section != null) {
            return section.getBlockState(getLocalCoord(x), getLocalCoord(y), getLocalCoord(z));
        }

        return Blocks.AIR.getDefaultState();
    }

    public FluidState getFluidState(BlockPos pos) {
        return this.getFluidState(pos.getX(), pos.getY(), pos.getZ());
    }

    public FluidState getFluidState(int x, int y, int z) {
        ChunkSection section = this.getChunkSection(getSectionCoord(x), getSectionCoord(y), getSectionCoord(z));

        if (section != null) {
            return section.getFluidState(getLocalCoord(x), getLocalCoord(y), getLocalCoord(z));
        }

        return Fluids.EMPTY.getDefaultState();
    }

    public WorldChunk getChunk(int x, int z) {
        if (ChunkPos.toLong(x, z) == this.prevPos) {
            return this.prev;
        }

        int iX = x - this.startX;
        int iZ = z - this.startZ;

        WorldChunk chunk;

        if (iX >= 0 && iX < LENGTH && iZ >= 0 && iZ < LENGTH) {
            chunk = this.chunks[index2(iX, iZ)];
        } else {
            chunk = this.getChunkFallback(x, z);
        }

        this.prev = chunk;
        this.prevPos = ChunkPos.toLong(x, z);

        return chunk;
    }

    public WorldChunk fetchChunk(int x, int z) {
        int iX = x - this.startX;
        int iZ = z - this.startZ;

        WorldChunk chunk = null;

        if (iX >= 0 && iX < LENGTH && iZ >= 0 && iZ < LENGTH) {
            chunk = this.chunks[index2(iX, iZ)];
        }

        if (chunk == null) {
            chunk = this.getChunkFallback(x, z);
        }

        return chunk;
    }

    private ChunkSection getChunkSection(int x, int y, int z) {
        if (y < 0 || y >= 16) {
            return null;
        }

        Chunk chunk = this.getChunk(x, z);

        if (chunk != null) {
            return chunk.getSectionArray()[y];
        }

        return null;
    }

    private WorldChunk getChunkFallback(int x, int z) {
        return this.chunkManager.getWorldChunk(x, z, false);
    }

    public boolean isRegionLoaded(BlockPos min, BlockPos max) {
        return this.isRegionLoaded(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
    }

    public boolean isRegionLoaded(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if (maxY < 0 || minY >= 256) {
            return false;
        }

        minX >>= 4;
        minZ >>= 4;
        maxX >>= 4;
        maxZ >>= 4;

        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                if (this.getChunk(x, z) == null) {
                    return false;
                }
            }
        }

        return true;
    }

    private static int index2(int x, int z) {
        return (x * LENGTH) + z;
    }
}
