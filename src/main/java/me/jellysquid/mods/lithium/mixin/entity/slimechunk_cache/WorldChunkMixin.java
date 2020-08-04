package me.jellysquid.mods.lithium.mixin.entity.slimechunk_cache;

import org.spongepowered.asm.mixin.Mixin;

import me.jellysquid.mods.lithium.common.chunk.ChunkWithSlimeTag;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin implements Chunk, ChunkWithSlimeTag{

    // Set the value to 2 for being unsure of a slime chunk.
    public int isSlime = 2;

    @Override
    public int isSlimeChunk() {
        return isSlime;
    }

    @Override
    public void setSlimeChunk(int value) {
        this.isSlime = value;
    }
}