package me.jellysquid.mods.lithium.common.block.redstone;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

/**
 * Provides a view of the world for getting/setting redstone wire states. This avoids going through the slow
 * World methods and will perform the absolute minimum amount of work to change a block in the world and notify clients.
 */
public class RedstoneBlockAccess {
    private final World world;
    private final ChunkManager chunkManager;

    private WorldChunk prev;
    private long prevPos;

    public RedstoneBlockAccess(World world) {
        this.world = world;
        this.chunkManager = world.getChunkManager();

        this.clear();
    }

    public BlockState getBlockState(BlockPos pos) {
        int y = pos.getY();

        if (y < 0 || y >= 256) {
            return Blocks.AIR.getDefaultState();
        }

        int x = pos.getX();
        int z = pos.getZ();

        WorldChunk chunk = this.getChunk(x >> 4, z >> 4);

        if (chunk != null) {
            ChunkSection section = chunk.getSectionArray()[y >> 4];

            if (section != null) {
                return section.getBlockState(x & 15, y & 15, z & 15);
            }
        }

        return Blocks.AIR.getDefaultState();
    }

    public void setBlockState(BlockPos pos, BlockState state) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        WorldChunk chunk = this.getChunk(x >> 4, z >> 4);

        if (chunk != null) {
            ChunkSection section = chunk.getSectionArray()[y >> 4];

            if (section != null) {
                section.setBlockState(x & 15, y & 15, z & 15, state);

                if (this.chunkManager instanceof ServerChunkManager) {
                    ((ServerChunkManager) this.chunkManager).markForUpdate(pos);
                }
            }
        }
    }

    private WorldChunk getChunk(int x, int z) {
        if (ChunkPos.toLong(x, z) == this.prevPos) {
            return this.prev;
        }

        this.prev = this.world.getChunk(x, z);
        this.prevPos = ChunkPos.toLong(x, z);

        return this.prev;
    }

    public void clear() {
        this.prevPos = Long.MIN_VALUE;
        this.prev = null;
    }
}
