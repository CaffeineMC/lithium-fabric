package me.jellysquid.mods.lithium.mixin.world.combined_heightmap_update;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Predicate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

@Mixin(Heightmap.class)
public interface HeightmapAccessor {
    @Invoker("setHeight")
    void callSet(int x, int z, int height);
    @Accessor("isOpaque")
    Predicate<BlockState> getBlockPredicate();
}
