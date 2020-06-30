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

import me.jellysquid.mods.lithium.common.util.collections.CollectionAsList;
import me.jellysquid.mods.lithium.common.world.NullBlockEntityList;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.function.Supplier;

/**
 * This mixin patch fixes the MC-117075 bug and optimizes block entity unloading & ticking.
 *
 * MC-117075 is due to the complexity of O(n) {@link ArrayList#removeAll(Collection)}, because it is
 * executed at the beginning of each tick in order to clear the lists of block entities from those
 * that should be unloaded.
 *
 * The bug reporter suggested a simple fix: replace the {@link World#blockEntities}
 * and World#tickingBlockEntities lists with {@link Set}, however, this seems to be not entirely
 * relevant. World#tickingBlockEntities is used to iterate tick function of each block entity and
 * {@link ArrayList} is best suited for this, so we can simply replace World#unloadedBlockEntities
 * with a {@link HashSet}, which will fix the problem of {@link ArrayList#removeAll(Collection)}
 * (since it uses the contains method and Sets has O(1) for it) and improve the speed of
 * add, addAll, remove and removeAll methods World#unloadedBlockEntities.
 *
 * I also noticed that, {@link World#blockEntities} copies the logic of World#tickingBlockEntities
 * slowing all these processes down by half. This patch removes it, which should reduce resource
 * consumption and increase the performance of block entity ticking.
 *
 * @author Maity
 */
@Mixin(World.class)
public class MixinWorld {
    @Shadow
    @Final
    @Mutable
    private List<BlockEntity> unloadedBlockEntities;

    @Shadow
    @Final
    @Mutable
    public List<BlockEntity> blockEntities;

    /**
     * Re-initialize block entity collections using custom adapters, which avoids errors with other mods
     * and supporting their work.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void reinitialize(MutableWorldProperties props, RegistryKey<World> worldKey, RegistryKey<DimensionType> dimensionKey, DimensionType type, Supplier<Profiler> profiler, boolean isDebugWorld, boolean isClient, long seed, CallbackInfo ci) {
        this.unloadedBlockEntities = new CollectionAsList<>(new HashSet<>());

        // This will "delete" this unnecessary list by overriding all methods with empty clones.
        this.blockEntities = new NullBlockEntityList<>();
    }
}