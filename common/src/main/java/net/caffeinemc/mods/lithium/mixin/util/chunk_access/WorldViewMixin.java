package net.caffeinemc.mods.lithium.mixin.util.chunk_access;

import net.caffeinemc.mods.lithium.common.world.ChunkView;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelReader.class)
public interface WorldViewMixin extends ChunkView {

    @Shadow
    @Nullable ChunkAccess getChunk(int var1, int var2, ChunkStatus var3, boolean var4);

    @Override
    default @Nullable ChunkAccess lithium$getLoadedChunk(int chunkX, int chunkZ) {
        return this.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
    }
}
