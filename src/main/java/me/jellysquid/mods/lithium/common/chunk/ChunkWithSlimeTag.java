package me.jellysquid.mods.lithium.common.chunk;

public interface ChunkWithSlimeTag {
    public boolean isSlime = false;

    public boolean isSlimeChunk();
    public void setSlimeChunk(boolean flag);
}