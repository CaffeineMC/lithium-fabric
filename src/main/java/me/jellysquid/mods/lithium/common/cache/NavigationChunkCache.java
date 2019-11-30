package me.jellysquid.mods.lithium.common.cache;

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

    private final int minX;
    private final int minZ;

    private final Chunk[] chunks;
    private final ChunkSection[] sections;

    private final World world;

    public NavigationChunkCache(World world, BlockPos min, BlockPos max) {
        this.world = world;

        this.minX = min.getX() >> 4;
        this.minZ = min.getZ() >> 4;

        int maxX = max.getX() >> 4;
        int maxZ = max.getZ() >> 4;

        int width = maxX - this.minX + 1;
        int height = maxZ - this.minZ + 1;

        this.chunks = new Chunk[width * height];
        this.sections = new ChunkSection[width * height * 16];

        for (int x = this.minX; x <= maxX; ++x) {
            for (int z = this.minZ; z <= maxZ; ++z) {
                Chunk chunk = world.getChunk(x, z, ChunkStatus.FULL, false);

                if (chunk == null) {
                    chunk = new EmptyChunk(this.world, new ChunkPos(x, z));
                }

                this.chunks[indexChunk(x - this.minX, z - this.minZ)] = chunk;

                for (int y = 0; y < 16; y++) {
                    ChunkSection section = chunk.getSectionArray()[y];

                    if (section == null) {
                        section = EMPTY_SECTION;
                    }

                    this.sections[indexSection(x - this.minX, y, z - this.minZ)] = section;
                }
            }
        }

        for (int x = min.getX() >> 4; x <= max.getX() >> 4; ++x) {
            for (int z = min.getZ() >> 4; z <= max.getZ() >> 4; ++z) {
                Chunk chunk = this.chunks[indexChunk(x - this.minX, z - this.minZ)];

                if (chunk != null && !chunk.method_12228(min.getY(), max.getY())) {

                    return;
                }
            }
        }
    }

    public int getLightLevel(BlockPos pos, int int_1) {
        return this.world.getLightLevel(pos, int_1);
    }

    public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus status, boolean flag) {
        int i = indexChunk(chunkX - this.minX, chunkZ - this.minZ);

        if (i >= 0 && i < this.chunks.length) {
            return this.chunks[i];
        } else {
            return new EmptyChunk(this.world, new ChunkPos(chunkX, chunkZ));
        }
    }

    public ChunkSection getChunkSection(int chunkX, int chunkY, int chunkZ) {
        int i = indexSection(chunkX - this.minX, chunkY, chunkZ - this.minZ);

        if (i >= 0 && i < this.sections.length) {
            return this.sections[i];
        } else {
            return EMPTY_SECTION;
        }
    }

    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        int i = indexChunk(chunkX - this.minX, chunkZ - this.minZ);

        return i >= 0 && i < this.chunks.length;
    }

    public BlockPos getTopPosition(Heightmap.Type type, BlockPos pos) {
        return this.world.getTopPosition(type, pos);
    }

    public int getTop(Heightmap.Type type, int x, int z) {
        return this.world.getTop(type, x, z);
    }

    public int getAmbientDarkness() {
        return this.world.getAmbientDarkness();
    }

    public WorldBorder getWorldBorder() {
        return this.world.getWorldBorder();
    }

    public boolean intersectsEntities(Entity entity, VoxelShape shape) {
        return true;
    }

    public boolean isClient() {
        return false;
    }

    public int getSeaLevel() {
        return this.world.getSeaLevel();
    }

    public Dimension getDimension() {
        return this.world.getDimension();
    }

    public BlockEntity getBlockEntity(BlockPos pos) {
        return this.getChunk(pos).getBlockEntity(pos);
    }

    public BlockState getBlockState(BlockPos pos) {
        if (World.isHeightInvalid(pos)) {
            return Blocks.AIR.getDefaultState();
        } else {
            return this.getChunkSection(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4)
                    .getBlockState(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
        }
    }

    public FluidState getFluidState(BlockPos pos) {
        if (World.isHeightInvalid(pos)) {
            return Fluids.EMPTY.getDefaultState();
        } else {
            return this.getChunkSection(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4)
                    .getFluidState(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
        }
    }

    public Biome getBiome(BlockPos pos) {
        return this.getChunk(pos).getBiome(pos);
    }

    public int getLightLevel(LightType type, BlockPos pos) {
        return this.world.getLightLevel(type, pos);
    }

    private static int indexChunk(int x, int z) {
        return x * z;
    }

    private static int indexSection(int x, int y, int z) {
        return (x * z * 16) + y;
    }

}
