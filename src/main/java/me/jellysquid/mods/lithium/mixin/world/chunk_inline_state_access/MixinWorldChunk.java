package me.jellysquid.mods.lithium.mixin.world.chunk_inline_state_access;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = WorldChunk.class, priority = 500)
public class MixinWorldChunk {
    private static final BlockState AIR = Blocks.AIR.getDefaultState();
    private static final FluidState EMPTY_FLUID = Fluids.EMPTY.getDefaultState();

    @Shadow
    @Final
    private ChunkSection[] sections;

    /**
     * @reason Help JVM to optimize code by reducing instructions
     * @author JellySquid, Maity
     */
    @Overwrite
    public BlockState getBlockState(BlockPos blockPos) {
        final int x = blockPos.getX();
        final int y = blockPos.getY();
        final int z = blockPos.getZ();

        if (!World.isHeightInvalid(y)) {
            final ChunkSection section = this.sections[y >> 4];

            if (section != null) {
                return section.getContainer().get(x & 15, y & 15, z & 15);
            }
        }

        return AIR;
    }

    /**
     * @reason Help JVM to optimize code by reducing instructions
     * @author JellySquid, Maity
     */
    @Overwrite
    public FluidState getFluidState(int x, int y, int z) {
        if (!World.isHeightInvalid(y)) {
            final ChunkSection section = this.sections[y >> 4];

            if (section != null) {
                return section.getContainer().get(x & 15, y & 15, z & 15).getFluidState();
            }
        }

        return EMPTY_FLUID;
    }
}