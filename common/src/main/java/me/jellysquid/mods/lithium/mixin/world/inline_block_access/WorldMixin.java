package me.jellysquid.mods.lithium.mixin.world.inline_block_access;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Level.class)
public abstract class WorldMixin implements LevelHeightAccessor {
    private static final BlockState OUTSIDE_WORLD_BLOCK = Blocks.VOID_AIR.defaultBlockState();
    private static final BlockState INSIDE_WORLD_DEFAULT_BLOCK = Blocks.AIR.defaultBlockState();

    @Shadow
    public abstract LevelChunk getChunk(int i, int j);

    /**
     * @reason Reduce method size to help the JVM inline, Avoid excess height limit checks
     * @author 2No2Name
     */
    @Overwrite
    public BlockState getBlockState(BlockPos pos) {
        LevelChunk worldChunk = this.getChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
        LevelChunkSection[] sections = worldChunk.getSections();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        int chunkY = this.getSectionIndex(y);
        //worldChunk.isEmpty() for EmptyChunk which is only VOID_AIR, used for client-side unloaded chunk
        if (chunkY < 0 || chunkY >= sections.length || worldChunk.isEmpty()) {
            return OUTSIDE_WORLD_BLOCK;
        }

        LevelChunkSection section = sections[chunkY];
        if (section == null || section.hasOnlyAir()) {
            return INSIDE_WORLD_DEFAULT_BLOCK;
        }
        return section.getBlockState(x & 15, y & 15, z & 15);
        //This code path is slower than with the extra world height limit check. Tradeoff in favor of the default path.
    }

    @Redirect(
            method = "getFluidState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;isOutsideBuildHeight(Lnet/minecraft/core/BlockPos;)Z"
            )
    )
    private boolean skipFluidHeightLimitTest(Level world, BlockPos pos) {
        return world.isOutsideBuildHeight(pos);
    }
}
