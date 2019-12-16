package me.jellysquid.mods.lithium.common.cache;

import me.jellysquid.mods.lithium.common.entity.ExtendedEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.ViewableWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.dimension.Dimension;

/**
 * Makes use a flattened array and direct access to chunk sections for improved performance when
 * compared to {@link net.minecraft.world.chunk.ChunkCache}.
 */
public class NavigationChunkCache implements ViewableWorld {
    private static final ChunkSection EMPTY_SECTION = new ChunkSection(0);

    private final int startX, startZ;
    private final int width, height;

    private final Chunk[] chunks;
    private final ChunkSection[] sections;

    private final World world;

    public NavigationChunkCache(Entity entity, World world, BlockPos min, BlockPos max) {
        EntityChunkCache cache = entity instanceof ExtendedEntity ? ((ExtendedEntity) entity).getEntityChunkCache() : null;

        this.world = world;

        this.startX = min.getX() >> 4;
        this.startZ = min.getZ() >> 4;

        int maxX = max.getX() >> 4;
        int maxZ = max.getZ() >> 4;

        this.width = maxX - this.startX;
        this.height = maxZ - this.startZ;

        this.chunks = new Chunk[(this.width + 1) * (this.height + 1)];
        this.sections = new ChunkSection[this.chunks.length * 16];

        for (int x = this.startX; x <= maxX; ++x) {
            for (int z = this.startZ; z <= maxZ; ++z) {
                Chunk chunk = cache != null ? cache.getChunk(x, z) : world.getChunk(x, z, ChunkStatus.FULL, false);

                if (chunk == null) {
                    chunk = new EmptyChunk(this.world, new ChunkPos(x, z));
                }

                int i = indexChunk(x - this.startX, z - this.startZ);

                this.chunks[i] = chunk;

                for (int j = 0; j < 16; j++) {
                    this.sections[(i * 16) + j] = chunk.getSectionArray()[j] == null ? EMPTY_SECTION : chunk.getSectionArray()[j];
                }
            }
        }
    }

    @Override
    public int getLightLevel(BlockPos pos, int int_1) {
        return this.world.getLightLevel(pos, int_1);
    }

    @Override
    public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus status, boolean flag) {
        int i = indexChunk(chunkX - this.startX, chunkZ - this.startZ);

        if (i >= 0 && i < this.chunks.length) {
            return this.chunks[i];
        } else {
            return new EmptyChunk(this.world, new ChunkPos(chunkX, chunkZ));
        }
    }

    public ChunkSection getChunkSection(int chunkX, int chunkY, int chunkZ) {
        if (chunkY < 0 || chunkY >= 16) {
            return EMPTY_SECTION;
        }

        int offsetX = chunkX - this.startX;
        int offsetZ = chunkZ - this.startZ;

        if (offsetX < 0 || offsetX >= this.width || offsetZ < 0 || offsetZ >= this.height) {
            return EMPTY_SECTION;
        }

        return this.sections[this.indexChunk(offsetX, chunkY, offsetZ)];
    }

    private int indexChunk(int x, int y, int z) {
        return (((x * this.width) + z) * 16) + y;
    }

    @Override
    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        int x = chunkX - this.startX;
        int z = chunkZ - this.startZ;

        return x >= 0 && x < this.width && z >= 0 && z < this.height;
    }

    @Override
    public BlockPos getTopPosition(Heightmap.Type type, BlockPos pos) {
        return this.world.getTopPosition(type, pos);
    }

    @Override
    public int getTop(Heightmap.Type type, int x, int z) {
        return this.world.getTop(type, x, z);
    }

    @Override
    public int getAmbientDarkness() {
        return this.world.getAmbientDarkness();
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.world.getWorldBorder();
    }

    @Override
    public boolean intersectsEntities(Entity entity, VoxelShape shape) {
        return true;
    }

    @Override
    public boolean isClient() {
        return false;
    }

    @Override
    public int getSeaLevel() {
        return this.world.getSeaLevel();
    }

    @Override
    public Dimension getDimension() {
        return this.world.getDimension();
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return this.getChunk(pos).getBlockEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (World.isHeightInvalid(pos)) {
            return Blocks.AIR.getDefaultState();
        }

        return this.getChunkSection(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4)
                .getBlockState(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        if (World.isHeightInvalid(pos)) {
            return Fluids.EMPTY.getDefaultState();
        }

        return this.getChunkSection(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4)
                .getFluidState(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return this.getChunk(pos).getBiome(pos);
    }

    @Override
    public int getLightLevel(LightType type, BlockPos pos) {
        return this.world.getLightLevel(type, pos);
    }

    private int indexChunk(int x, int z) {
        return (x * this.width) + z;
    }
}
