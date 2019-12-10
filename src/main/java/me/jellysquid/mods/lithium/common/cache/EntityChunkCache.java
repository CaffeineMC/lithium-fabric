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
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

public class EntityChunkCache extends AbstractCachedAccess {
    private static final int DIMENSIONS = 2;
    private static final int CHUNKS = DIMENSIONS * DIMENSIONS;

    private WorldChunk[] chunks = new WorldChunk[CHUNKS];
    private WorldChunk[] swap = new WorldChunk[CHUNKS];
    private final World world;
    private int lastXCorner, lastZCorner;

    public EntityChunkCache(World world) {
        this.world = world;
    }

    public void updateChunks(Box hitbox) {
        int baseX = MathHelper.floor(hitbox.minX / 16);
        int baseZ = MathHelper.floor(hitbox.minZ / 16);
        int maxX = MathHelper.floor(hitbox.maxX / 16);
        int maxZ = MathHelper.floor(hitbox.maxZ / 16);

        for (int x = baseX; x < maxX; x++) {
            for (int z = baseZ; z < maxZ; z++) {
                this.swap[(x-baseX) * DIMENSIONS + (z-baseZ)] = this.getWorldChunk(x, z);
            }
        }

        WorldChunk[] former = this.chunks;
        this.chunks = this.swap;
        this.swap = former;

        this.lastXCorner = baseX;
        this.lastZCorner = baseZ;
    }

    public WorldChunk getWorldChunk(Entity entity) {
        return this.getWorldChunk(entity.chunkX, entity.chunkZ);
    }

    public WorldChunk getWorldChunk(BlockPos pos) {
        return this.getWorldChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public WorldChunk getWorldChunk(int chunkX, int chunkZ) {
        int offsetX = chunkX - this.lastXCorner;
        int offsetZ = chunkZ - this.lastZCorner;
        if (offsetX >= 0 && offsetZ >= 0 && offsetX < DIMENSIONS && offsetZ < DIMENSIONS) {
            WorldChunk chunk = this.chunks[offsetX * DIMENSIONS + offsetZ];
            if(chunk == null)
                return this.world.method_8497(chunkX, chunkZ);
            return chunk;
        }
        return this.world.getChunkManager().getWorldChunk(chunkX, chunkZ, true);
    }

    private ChunkSection getChunkSection(int x, int y, int z) {
        if (y < 0 || y >= 16) {
            return null;
        }

        Chunk chunk = this.getWorldChunk(x, z);

        if (chunk == null) {
            chunk = this.world.getChunk(x, z);
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

    public World getWorld() {
        return this.world;
    }
}
