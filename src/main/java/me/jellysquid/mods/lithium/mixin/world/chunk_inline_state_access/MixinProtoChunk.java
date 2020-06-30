/*
 * Turtle Mod
 * Copyright (C) 2020 Maity
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package me.jellysquid.mods.lithium.mixin.world.chunk_inline_state_access;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ProtoChunk.class, priority = 500)
public class MixinProtoChunk {
    private static final BlockState AIR = Blocks.AIR.getDefaultState();
    private static final BlockState VOID_AIR = Blocks.VOID_AIR.getDefaultState();

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

        if (y < 0 || y >= 256) { // [VanillaCopy] ProtoChunk#getFluidState
            return VOID_AIR;
        } else {
            ChunkSection section = this.sections[y >> 4];

            if (section != null) {
                return section.getBlockState(x & 15, y & 15, z & 15);
            }
        }

        return AIR;
    }

    /**
     * @reason Help JVM to optimize code by reducing instructions
     * @author JellySquid, Maity
     */
    @Overwrite
    public FluidState getFluidState(BlockPos blockPos) {
        final int x = blockPos.getX();
        final int y = blockPos.getY();
        final int z = blockPos.getZ();

        if (y >= 0 && y <= 256) {
            ChunkSection section = this.sections[y >> 4];

            if (section != null) {
                return section.getFluidState(x & 15, y & 15, z & 15);
            }
        }

        return EMPTY_FLUID;
    }
}
