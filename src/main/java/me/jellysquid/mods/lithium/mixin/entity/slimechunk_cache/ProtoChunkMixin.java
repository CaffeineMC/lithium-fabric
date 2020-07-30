package me.jellysquid.mods.lithium.mixin.entity.slimechunk_cache;

import org.spongepowered.asm.mixin.Mixin;

import me.jellysquid.mods.lithium.common.chunk.ChunkWithSlimeTag;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;

@Mixin(ProtoChunk.class)
public abstract class ProtoChunkMixin implements Chunk, ChunkWithSlimeTag{

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