package net.caffeinemc.mods.lithium.mixin.util.chunk_access;

import net.caffeinemc.mods.lithium.common.world.ChunkView;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PathNavigationRegion.class)
public abstract class PathNavigationRegionMixin implements ChunkView {

    @Shadow
    protected abstract ChunkAccess getChunk(int chunkX, int chunkZ);

    @Override
    public @Nullable ChunkAccess lithium$getLoadedChunk(int chunkX, int chunkZ) {
        return this.getChunk(chunkX, chunkZ);
    }
}
