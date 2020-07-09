package me.jellysquid.mods.lithium.mixin.world.mob_spawning;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StructureAccessor.class)
public abstract class StructureAccessorMixin {
    @Shadow
    @Final
    private WorldAccess world;

    /**
     * @reason Avoid heavily nested stream code and object allocations where possible
     * @author JellySquid
     */
    @Overwrite
    public StructureStart<?> method_28388(BlockPos blockPos, boolean fine, StructureFeature<?> feature) {
        Chunk originChunk = this.world.getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4, ChunkStatus.STRUCTURE_REFERENCES);

        LongSet references = originChunk.getStructureReferences(feature);
        LongIterator iterator = references.iterator();

        while (iterator.hasNext()) {
            long pos = iterator.nextLong();

            Chunk chunk = this.world.getChunk(ChunkPos.getPackedX(pos), ChunkPos.getPackedZ(pos), ChunkStatus.STRUCTURE_STARTS);
            StructureStart<?> structure = chunk.getStructureStart(feature);

            if (structure == null || !structure.hasChildren() || !structure.getBoundingBox().contains(blockPos)) {
                continue;
            }

            if (!fine || this.anyPieceContainsPosition(structure, blockPos)) {
                return structure;
            }
        }

        return StructureStart.DEFAULT;
    }

    private boolean anyPieceContainsPosition(StructureStart<?> structure, BlockPos blockPos) {
        for (StructurePiece piece : structure.getChildren()) {
            if (piece.getBoundingBox().contains(blockPos)) {
                return true;
            }
        }

        return false;
    }
}
