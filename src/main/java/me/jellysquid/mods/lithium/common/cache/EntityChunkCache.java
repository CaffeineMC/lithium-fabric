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

    private WorldChunk[] cache = new WorldChunk[LENGTH * LENGTH];

    private ChunkManager chunkManager;

    private int startX, startZ;

    private boolean isCacheEmpty = true;

    public EntityChunkCache(Entity entity) {
        this.entity = entity;
        this.chunkManager = entity.getEntityWorld().getChunkManager();
    }

    public World getWorld() {
        return this.entity.getEntityWorld();
    }

    public void updateChunks(Box box) {
        int startX = getSectionCoord(MathHelper.floor(box.x1)) - RADIUS;
        int startZ = getSectionCoord(MathHelper.floor(box.z1)) - RADIUS;

        ChunkManager chunkManager = this.getWorld().getChunkManager();

        // If the world/chunk manager has changed, we need to reset
        if (chunkManager != this.chunkManager) {
            Arrays.fill(this.cache, null);

            this.isCacheEmpty = true;
        } else {
            // If we're not watching any new chunks, we have no need to update anything
            if (startX == this.startX && startZ == this.startZ) {
                return;
            }
        }

        if (!this.isCacheEmpty) {
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

        this.isCacheEmpty = false;
    }

    private ChunkSection getChunkSection(int x, int y, int z, boolean required) {
        if (y < 0 || y >= 16) {
            return null;
        }

        Chunk chunk = required ? this.getChunk(x, z) : this.getCachedChunk(x, z);

        if (chunk != null) {
            return chunk.getSectionArray()[y];
        }

        return null;
    }

    public BlockState getBlockState(BlockPos pos) {
        return this.getBlockState(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockState getBlockState(int x, int y, int z) {
        ChunkSection section = this.getChunkSection(getSectionCoord(x), getSectionCoord(y), getSectionCoord(z), true);

        if (section != null) {
            return section.getBlockState(getLocalCoord(x), getLocalCoord(y), getLocalCoord(z));
        }

        return Blocks.AIR.getDefaultState();
    }

    public FluidState getFluidState(BlockPos pos, boolean required) {
        return this.getFluidState(pos.getX(), pos.getY(), pos.getZ(), required);
    }

    public FluidState getFluidState(BlockPos pos) {
        return this.getFluidState(pos.getX(), pos.getY(), pos.getZ(), true);
    }

    public FluidState getFluidState(int x, int y, int z) {
        return this.getFluidState(x, y, z, true);
    }

    public FluidState getFluidState(int x, int y, int z, boolean required) {
        ChunkSection section = this.getChunkSection(getSectionCoord(x), getSectionCoord(y), getSectionCoord(z), required);

        if (section != null) {
            return section.getFluidState(getLocalCoord(x), getLocalCoord(y), getLocalCoord(z));
        }

        return Fluids.EMPTY.getDefaultState();
    }

    public WorldChunk getChunk(int x, int z) {
        int iX = x - this.startX;
        int iZ = z - this.startZ;

        if (iX >= 0 && iX < LENGTH && iZ >= 0 && iZ < LENGTH) {
            int i = (iX * LENGTH) + iZ;

            WorldChunk chunk = this.cache[i];

            if (chunk == null) {
                this.cache[i] = chunk = this.chunkManager.getWorldChunk(x, z, false);
            }

            return chunk;
        }

        return this.chunkManager.getWorldChunk(x, z, true);
    }

    public WorldChunk getCachedChunk(int x, int z) {
        int iX = x - this.startX;
        int iZ = z - this.startZ;

        if (iX >= 0 && iX < LENGTH && iZ >= 0 && iZ < LENGTH) {
            return this.cache[(iX * LENGTH) + iZ];
        }

        return null;
    }
}
