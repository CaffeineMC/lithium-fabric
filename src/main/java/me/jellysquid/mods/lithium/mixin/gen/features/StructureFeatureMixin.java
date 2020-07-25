package me.jellysquid.mods.lithium.mixin.gen.features;

import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(StructureFeature.class)
public class StructureFeatureMixin {

    /**
     * @reason Why generate an empty chunk to check for a structure if
     * the chunk's biome cannot generate the structure anyway?
     * Checking the biome first = SPEED!
     * @author TelepathicGrunt
     */
    @Overwrite
    public BlockPos locateStructure(WorldView worldView, StructureAccessor structureAccessor, BlockPos blockPos, int i, boolean skipExistingChunks, long l, StructureConfig structureConfig) {
        int j = structureConfig.getSpacing();
        int k = blockPos.getX() >> 4;
        int m = blockPos.getZ() >> 4;
        int n = 0;
        StructureFeature thisStructure = ((StructureFeature) (Object) this);

        for(ChunkRandom chunkRandom = new ChunkRandom(); n <= i; ++n) {
            for(int o = -n; o <= n; ++o) {
                boolean bl = o == -n || o == n;

                for(int p = -n; p <= n; ++p) {
                    boolean bl2 = p == -n || p == n;
                    if (bl || bl2) {
                        int q = k + j * o;
                        int r = m + j * p;
                        ChunkPos chunkPos = thisStructure.method_27218(structureConfig, l, chunkRandom, q, r);
                        if(worldView.getBiomeForNoiseGen(chunkPos.x << 2, 60, chunkPos.z << 2).hasStructureFeature(thisStructure)) {
                            Chunk chunk = worldView.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS);
                            StructureStart<?> structureStart = structureAccessor.getStructureStart(ChunkSectionPos.from(chunk.getPos(), 0), thisStructure, chunk);
                            if (structureStart != null && structureStart.hasChildren()) {
                                if (skipExistingChunks && structureStart.isInExistingChunk()) {
                                    structureStart.incrementReferences();
                                    return structureStart.getPos();
                                }

                                if (!skipExistingChunks) {
                                    return structureStart.getPos();
                                }
                            }
                        }

                        if (n == 0) {
                            break;
                        }
                    }
                }

                if (n == 0) {
                    break;
                }
            }
        }

        return null;
    }
}
