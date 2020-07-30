package me.jellysquid.mods.lithium.common.chunk;

public interface ChunkWithSlimeTag {
    public boolean isSlime = true;

    public boolean isSlimeChunk();
    public void setSlimeChunk(boolean flag);
}