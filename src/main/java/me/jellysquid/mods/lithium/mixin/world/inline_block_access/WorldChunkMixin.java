package me.jellysquid.mods.lithium.mixin.world.inline_block_access;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = LevelChunk.class, priority = 500)
public abstract class WorldChunkMixin extends ChunkAccess {
    private static final BlockState DEFAULT_BLOCK_STATE = Blocks.AIR.defaultBlockState();
    private static final FluidState DEFAULT_FLUID_STATE = Fluids.EMPTY.defaultFluidState();

    public WorldChunkMixin(ChunkPos pos, UpgradeData upgradeData, LevelHeightAccessor heightLimitView, Registry<Biome> biome, long inhabitedTime, @Nullable LevelChunkSection[] sectionArrayInitializer, @Nullable BlendingData blendingData) {
        super(pos, upgradeData, heightLimitView, biome, inhabitedTime, sectionArrayInitializer, blendingData);
    }

    /**
     * @reason Reduce method size to help the JVM inline
     * @author JellySquid
     */
    @Overwrite
    public BlockState getBlockState(BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        int chunkY = this.getSectionIndex(y);
        LevelChunkSection[] sectionArray = this.getSections();
        if (chunkY >= 0 && chunkY < sectionArray.length) {
            LevelChunkSection section = sectionArray[chunkY];

            //checking isEmpty cannot be skipped here. https://bugs.mojang.com/browse/MC-232360
            // Chunk Sections that only contain air and cave_air are treated as empty
            if (!section.hasOnlyAir()) {
                return section.getBlockState(x & 15, y & 15, z & 15);
            }
        }

        return DEFAULT_BLOCK_STATE;
    }

    /**
     * @reason Reduce method size to help the JVM inline
     * @author JellySquid, Maity
     */
    @Overwrite
    public FluidState getFluidState(int x, int y, int z) {
        int chunkY = this.getSectionIndex(y);
        LevelChunkSection[] sectionArray = this.getSections();
        if (chunkY >= 0 && chunkY < sectionArray.length) {
            LevelChunkSection section = sectionArray[chunkY];
            return section.getFluidState(x & 15, y & 15, z & 15);
        }

        return DEFAULT_FLUID_STATE;
    }
}
