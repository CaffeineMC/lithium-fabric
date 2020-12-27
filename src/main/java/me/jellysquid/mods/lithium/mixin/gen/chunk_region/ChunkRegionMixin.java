package me.jellysquid.mods.lithium.mixin.gen.chunk_region;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChunkRegion.class)
public abstract class ChunkRegionMixin {
    @Shadow
    @Final
    private ChunkPos lowerCorner;

    @Shadow
    @Final
    private int width;

    // Array view of the chunks in the region to avoid an unnecessary de-reference
    private Chunk[] chunksArr;

    // The starting position of this region
    private int minChunkX, minChunkZ;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(ServerWorld world, List<Chunk> chunks, CallbackInfo ci) {
        this.minChunkX = this.lowerCorner.x;
        this.minChunkZ = this.lowerCorner.z;

        this.chunksArr = chunks.toArray(new Chunk[0]);
    }

    /**
     * @reason Avoid pointer de-referencing, make method easier to inline
     * @author JellySquid
     */
    @Overwrite
    public BlockState getBlockState(BlockPos pos) {
        int x = (pos.getX() >> 4) - this.minChunkX;
        int z = (pos.getZ() >> 4) - this.minChunkZ;
        int w = this.width;

        if (x >= 0 && z >= 0 && x < w && z < w) {
            return this.chunksArr[x + z * w].getBlockState(pos);
        } else {
            throw new NullPointerException("No chunk exists at " + new ChunkPos(pos));
        }
    }

    /**
     * @reason Use the chunk array for faster access
     */
    @Redirect(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/ChunkRegion;getChunk(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/chunk/Chunk;"), remap = true)
    private Chunk fastGetChunk(ChunkRegion world, BlockPos pos) {
        int x = (pos.getX() >> 4) - this.minChunkX;
        int z = (pos.getZ() >> 4) - this.minChunkZ;
        int w = this.width;

        if (x >= 0 && z >= 0 && x < w && z < w) {
            return this.chunksArr[x + z * w];
        } else {
            throw new NullPointerException("No chunk exists at " + new ChunkPos(pos));
        }
    }

    /**
     * @reason Avoid pointer de-referencing, make method easier to inline
     * @author SuperCoder79
     */
    @Overwrite
    public int getTopY(Heightmap.Type heightmap, int x, int z) {
        int chunkX = (x >> 4) - this.minChunkX;
        int chunkZ = (z >> 4) - this.minChunkZ;
        int w = this.width;

        if (chunkX >= 0 && chunkZ >= 0 && chunkX < w && chunkZ < w) {
            return this.chunksArr[chunkX + chunkZ * w].sampleHeightmap(heightmap, x & 15, z & 15) + 1;
        } else {
            throw new NullPointerException("No chunk exists at " + new ChunkPos(chunkX, chunkZ));
        }
    }

    /**
     * @reason Use our block fetch function
     * @author JellySquid
     */
    @Overwrite
    public FluidState getFluidState(BlockPos pos) {
        return this.getBlockState(pos).getFluidState();
    }
}
