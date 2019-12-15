package me.jellysquid.mods.lithium.common.cache;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Arrays;

import static net.minecraft.util.math.ChunkSectionPos.toChunkCoord;
import static net.minecraft.util.math.ChunkSectionPos.toLocalCoord;

/**
 * Maintains a cached collection of chunks around an entity. This allows for much faster access to nearby chunks for
 * many entity related functions.
 */
public class EntityChunkCache {
    private static final int RADIUS = 1;
    private static final int LENGTH = (RADIUS * 2) + 1;

    private final Entity entity;

    private WorldChunk[] cache = new WorldChunk[LENGTH * LENGTH];

    private ChunkManager chunkManager;

    private int startX, startZ;

    private boolean isCacheEnabled = false;

    public EntityChunkCache(Entity entity) {
        this.entity = entity;
        this.chunkManager = entity.getEntityWorld().getChunkManager();
    }

    public World getWorld() {
        return this.entity.getEntityWorld();
    }

    public void updateChunks(Box box) {
        int startX = toChunkCoord(MathHelper.floor(box.minX - 16.0D)) - RADIUS;
        int startZ = toChunkCoord(MathHelper.floor(box.minZ - 16.0D)) - RADIUS;

        ChunkManager chunkManager = this.getWorld().getChunkManager();

        // If the world/chunk manager has changed, we need to reset
        if (chunkManager != this.chunkManager) {
            Arrays.fill(this.cache, null);
        } else {
            // If we're not watching any new chunks, we have no need to update anything
            if (startX == this.startX && startZ == this.startZ) {
                return;
            }
        }

        if (this.isCacheEnabled) {
            WorldChunk[] cache = new WorldChunk[LENGTH * LENGTH];

            for (int x = 0; x < LENGTH; x++) {
                for (int z = 0; z < LENGTH; z++) {
                    cache[(x * LENGTH) + z] = this.getCachedChunk(startX + x, startZ + z);
                }
            }

            this.cache = cache;
        }

        this.startX = startX;
        this.startZ = startZ;
        this.chunkManager = chunkManager;

        this.isCacheEnabled = true;
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

    public BlockState getBlockState(BlockPos pos) {
        return this.getBlockState(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockState getBlockState(int x, int y, int z) {
        ChunkSection section = this.getChunkSection(toChunkCoord(x), toChunkCoord(y), toChunkCoord(z));

        if (section != null) {
            return section.getBlockState(toLocalCoord(x), toLocalCoord(y), toLocalCoord(z));
        }

        return Blocks.AIR.getDefaultState();
    }

    public FluidState getFluidState(BlockPos pos) {
        return this.getFluidState(pos.getX(), pos.getY(), pos.getZ());
    }

    public FluidState getFluidState(int x, int y, int z) {
        ChunkSection section = this.getChunkSection(toChunkCoord(x), toChunkCoord(y), toChunkCoord(z));

        if (section != null) {
            return section.getFluidState(toLocalCoord(x), toLocalCoord(y), toLocalCoord(z));
        }

        return Fluids.EMPTY.getDefaultState();
    }

    // We can avoid performing a lookup if the map is empty, which can sometimes happen.
    private WorldChunk getChunk(int x, int z) {
        int iX = x - this.startX;
        int iZ = z - this.startZ;

        if (iX >= 0 && iX < LENGTH && iZ >= 0 && iZ < LENGTH) {
            int i = (iX * LENGTH) + iZ;

            WorldChunk chunk = this.cache[i];

            if (chunk == null) {
                this.cache[i] = chunk = this.chunkManager.getWorldChunk(x, z, true);
            }

            return chunk;
        }

        return this.chunkManager.getWorldChunk(x, z, true);
    }

    // We can avoid performing a lookup if the map is empty, which can sometimes happen.
    private WorldChunk getCachedChunk(int x, int z) {
        int iX = x - this.startX;
        int iZ = z - this.startZ;

        if (iX >= 0 && iX < LENGTH && iZ >= 0 && iZ < LENGTH) {
            return this.cache[(iX * LENGTH) + iZ];
        }

        return null;
    }
}
