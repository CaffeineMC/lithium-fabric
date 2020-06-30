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

import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PalettedContainer.class)
public class MixinPalettedContainer<T> {
    @Shadow
    @Final
    private T field_12935;
    @Shadow
    private PackedIntegerArray data;
    @Shadow
    private Palette<T> palette;

    /**
     * @reason Help JVM to optimize code by reducing instructions
     * @author Maity
     */
    @Overwrite
    public T get(int x, int y, int z) {
        // this.get(toIndex(x, y, z))
        T o = this.palette.getByIndex(this.data.get(y << 8 | z << 4 | x));
        return o == null ? this.field_12935 : o;
    }
}
