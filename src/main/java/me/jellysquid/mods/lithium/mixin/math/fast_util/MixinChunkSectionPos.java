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
package me.jellysquid.mods.lithium.mixin.math.fast_util;

import net.minecraft.util.math.ChunkSectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkSectionPos.class)
public class MixinChunkSectionPos {
    /**
     * @reason Better inlining & reduce bytecode instructions
     * @author Maity
     */
    @Overwrite
    public static long asLong(int x, int y, int z) {
        return (((long) x & 4194303L) << 42) | (((long) y & 1048575L)) | (((long) z & 4194303L) << 20);
    }

    @Shadow
    public static native int getSectionCoord(int i);
}
