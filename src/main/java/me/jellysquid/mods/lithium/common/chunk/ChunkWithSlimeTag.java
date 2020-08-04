package me.jellysquid.mods.lithium.common.chunk;

public interface ChunkWithSlimeTag {
    public boolean isSlime = true;

    public int isSlimeChunk();
    public void setSlimeChunk(int flag);
}