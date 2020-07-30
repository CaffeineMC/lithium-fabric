package me.jellysquid.mods.lithium.mixin.entity.slimechunk_cache;

import org.spongepowered.asm.mixin.Mixin;

import me.jellysquid.mods.lithium.common.chunk.ChunkWithSlimeTag;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin implements Chunk, ChunkWithSlimeTag{

    public boolean isSlime = true;

    @Override
    public boolean isSlimeChunk() {
        return isSlime;
    }

    @Override
    public void setSlimeChunk(boolean flag) {
        this.isSlime = flag;
    }
}