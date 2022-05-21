package me.jellysquid.mods.lithium.common.util.tuples;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;

//Y values use coordinates, not indices (y=0 -> chunkY=0)
//upper bounds are EXCLUSIVE
public record WorldSectionBox(World world, int chunkX1, int chunkY1, int chunkZ1, int chunkX2, int chunkY2,
                              int chunkZ2) {
    public static WorldSectionBox entityAccessBox(World world, Box box) {
        int minX = ChunkSectionPos.getSectionCoord(box.minX - 2.0D);
        int minY = ChunkSectionPos.getSectionCoord(box.minY - 4.0D);
        int minZ = ChunkSectionPos.getSectionCoord(box.minZ - 2.0D);
        int maxX = ChunkSectionPos.getSectionCoord(box.maxX + 2.0D) + 1;
        int maxY = ChunkSectionPos.getSectionCoord(box.maxY) + 1;
        int maxZ = ChunkSectionPos.getSectionCoord(box.maxZ + 2.0D) + 1;
        return new WorldSectionBox(world, minX, minY, minZ, maxX, maxY, maxZ);
    }

    public int numSections() {
        return (this.chunkX2 - this.chunkX1) * (this.chunkY2 - this.chunkY1) * (this.chunkZ2 - this.chunkZ1);
    }
}
