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
package me.jellysquid.mods.lithium.mixin.world.tile_unloading;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld extends World {
    private MixinServerWorld(MutableWorldProperties props, RegistryKey<World> worldKey, RegistryKey<DimensionType> dimensionKey, DimensionType type, Supplier<Profiler> profiler, boolean isDebugWorld, boolean isClient, long seed) {
        super(props, worldKey, dimensionKey, type, profiler, isDebugWorld, isClient, seed);
    }

    @Redirect(method = "dump", at = @At(value = "INVOKE",
            target = "Ljava/util/List;size()I"))
    private int changeCollectionSizeLink(List<BlockEntity> blockEntities) {
        return this.tickingBlockEntities.size();
    }

    @Redirect(method = "dumpBlockEntities", at = @At(value = "INVOKE",
            target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    private Iterator<BlockEntity> changeCollectionIteratorLink(List<BlockEntity> blockEntities) {
        return this.tickingBlockEntities.iterator();
    }
}
