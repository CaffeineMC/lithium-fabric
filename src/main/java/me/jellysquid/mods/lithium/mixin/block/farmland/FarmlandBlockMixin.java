package me.jellysquid.mods.lithium.mixin.block.farmland;

import net.minecraft.block.FarmlandBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin {

    // Optimize FarmlandBlock nearby water lookup
    private static boolean isWaterNearby(WorldView world, BlockPos pos) {
        int posX = pos.getX();
        int posY = pos.getY();
        int posZ = pos.getZ();

        for (int dz = -4; dz <= 4; ++dz) {
            int z = dz + posZ;
            for (int dx = -4; dx <= 4; ++dx) {
                int x = posX + dx;
                for (int dy = 0; dy <= 1; ++dy) {
                    Chunk chunk = world.getChunk(x >> 4, z >> 4);
                    FluidState fluid = chunk.getBlockState(new BlockPos(x, dy + posY, z)).getFluidState();
                    if (fluid.isIn(FluidTags.WATER)) return true;
                }
            }
        }
        return false;
    }
}
