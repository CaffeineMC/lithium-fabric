package me.jellysquid.mods.lithium.common.util.tuples;

import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

//Y values use coordinates, not indices (y=0 -> chunkY=0)
//upper bounds are EXCLUSIVE
public record WorldSectionBox(Level world, int chunkX1, int chunkY1, int chunkZ1, int chunkX2, int chunkY2,
                              int chunkZ2) {
    public static WorldSectionBox entityAccessBox(Level world, AABB box) {
        int minX = SectionPos.posToSectionCoord(box.minX - 2.0D);
        int minY = SectionPos.posToSectionCoord(box.minY - 4.0D);
        int minZ = SectionPos.posToSectionCoord(box.minZ - 2.0D);
        int maxX = SectionPos.posToSectionCoord(box.maxX + 2.0D) + 1;
        int maxY = SectionPos.posToSectionCoord(box.maxY) + 1;
        int maxZ = SectionPos.posToSectionCoord(box.maxZ + 2.0D) + 1;
        return new WorldSectionBox(world, minX, minY, minZ, maxX, maxY, maxZ);
    }

    //Relevant block box: Entity hitbox expanded to all blocks it touches. Then expand the resulting box by 1 block in each direction.
    //Include all chunk sections that contain blocks inside the expanded box.
    public static WorldSectionBox relevantExpandedBlocksBox(Level world, AABB box) {
        int minX = SectionPos.blockToSectionCoord(Mth.floor(box.minX) - 1);
        int minY = SectionPos.blockToSectionCoord(Mth.floor(box.minY) - 1);
        int minZ = SectionPos.blockToSectionCoord(Mth.floor(box.minZ) - 1);
        int maxX = SectionPos.blockToSectionCoord(Mth.floor(box.maxX) + 1) + 1;
        int maxY = SectionPos.blockToSectionCoord(Mth.floor(box.maxY) + 1) + 1;
        int maxZ = SectionPos.blockToSectionCoord(Mth.floor(box.maxZ) + 1) + 1;
        return new WorldSectionBox(world, minX, minY, minZ, maxX, maxY, maxZ);
    }
    //Like relevant blocks, but not expanded, because fluids never exceed the 1x1x1 volume of a block
    public static WorldSectionBox relevantFluidBox(Level world, AABB box) {
        int minX = SectionPos.blockToSectionCoord(Mth.floor(box.minX));
        int minY = SectionPos.blockToSectionCoord(Mth.floor(box.minY));
        int minZ = SectionPos.blockToSectionCoord(Mth.floor(box.minZ));
        int maxX = SectionPos.blockToSectionCoord(Mth.floor(box.maxX)) + 1;
        int maxY = SectionPos.blockToSectionCoord(Mth.floor(box.maxY)) + 1;
        int maxZ = SectionPos.blockToSectionCoord(Mth.floor(box.maxZ)) + 1;
        return new WorldSectionBox(world, minX, minY, minZ, maxX, maxY, maxZ);
    }

    public int numSections() {
        return (this.chunkX2 - this.chunkX1) * (this.chunkY2 - this.chunkY1) * (this.chunkZ2 - this.chunkZ1);
    }

    public boolean matchesRelevantBlocksBox(AABB box) {
        return SectionPos.blockToSectionCoord(Mth.floor(box.minX) - 1) == this.chunkX1 &&
                SectionPos.blockToSectionCoord(Mth.floor(box.minY) - 1) == this.chunkY1 &&
                SectionPos.blockToSectionCoord(Mth.floor(box.minZ) - 1) == this.chunkZ1 &&
                SectionPos.blockToSectionCoord(Mth.ceil(box.maxX) + 1) + 1 == this.chunkX2 &&
                SectionPos.blockToSectionCoord(Mth.ceil(box.maxY) + 1) + 1 == this.chunkY2 &&
                SectionPos.blockToSectionCoord(Mth.ceil(box.maxZ) + 1) + 1 == this.chunkZ2;
    }

}
