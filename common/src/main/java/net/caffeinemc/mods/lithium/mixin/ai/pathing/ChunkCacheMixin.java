package net.caffeinemc.mods.lithium.mixin.ai.pathing;

import net.caffeinemc.mods.lithium.common.util.Pos;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The hottest part of path-finding is reading blocks out from the world. This patch makes a number of changes to
 * avoid slow paths in the game and to better inline code. In testing, it shows a small improvement in path-finding
 * code.
 */
@Mixin(PathNavigationRegion.class)
public abstract class ChunkCacheMixin implements BlockGetter {
    private static final BlockState DEFAULT_BLOCK = Blocks.AIR.defaultBlockState();

    @Shadow
    @Final
    protected ChunkAccess[][] chunks;

    @Shadow
    @Final
    protected int centerX; //This is minX, not centerX!

    @Shadow
    @Final
    protected int centerZ; //This is minZ, not centerZ!

    @Shadow
    @Final
    protected Level level;
    // A 1D view of the chunks available to this cache
    private ChunkAccess[] chunksFlat;

    // The x/z length of this cache
    private int xLen, zLen;

    private int bottomY, topY;

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)V", at = @At("RETURN"))
    private void init(Level world, BlockPos minPos, BlockPos maxPos, CallbackInfo ci) {
        this.xLen = 1 + (Pos.ChunkCoord.fromBlockCoord(maxPos.getX())) - (Pos.ChunkCoord.fromBlockCoord(minPos.getX()));
        this.zLen = 1 + (Pos.ChunkCoord.fromBlockCoord(maxPos.getZ())) - (Pos.ChunkCoord.fromBlockCoord(minPos.getZ()));

        this.chunksFlat = new ChunkAccess[this.xLen * this.zLen];

        // Flatten the 2D chunk array into our 1D array
        for (int x = 0; x < this.xLen; x++) {
            System.arraycopy(this.chunks[x], 0, this.chunksFlat, x * this.zLen, this.zLen);
        }

        this.bottomY = this.getMinBuildHeight();
        this.topY = this.getMaxBuildHeight();
    }

    /**
     * @reason Use optimized function
     * @author JellySquid
     */
    @Overwrite
    public BlockState getBlockState(BlockPos pos) {
        int y = pos.getY();

        if (!(y < this.bottomY || y >= this.topY)) {
            int x = pos.getX();
            int z = pos.getZ();

            int chunkX = (Pos.ChunkCoord.fromBlockCoord(x)) - this.centerX;
            int chunkZ = (Pos.ChunkCoord.fromBlockCoord(z)) - this.centerZ;

            if (chunkX >= 0 && chunkX < this.xLen && chunkZ >= 0 && chunkZ < this.zLen) {
                ChunkAccess chunk = this.chunksFlat[(chunkX * this.zLen) + chunkZ];

                // Avoid going through Chunk#getBlockState
                if (chunk != null) {
                    LevelChunkSection section = chunk.getSections()[Pos.SectionYIndex.fromBlockCoord(this, y)];

                    if (section != null) {
                        return section.getBlockState(x & 15, y & 15, z & 15);
                    }
                }
            }
        }

        return DEFAULT_BLOCK;
    }

    /**
     * @reason Use optimized function
     * @author JellySquid
     */
    @Overwrite
    public FluidState getFluidState(BlockPos pos) {
        return this.getBlockState(pos).getFluidState();
    }
}


