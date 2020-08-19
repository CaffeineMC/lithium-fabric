package me.jellysquid.mods.lithium.mixin.gen.features;

import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.StructureHolder;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.feature.StructureFeature;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * Why generate an empty chunk to check for a structure if the chunk's biome cannot generate the
 * structure anyway? Checking the biome first = SPEED!
 * @author TelepathicGrunt
 */
@Mixin(StructureFeature.class)
public class StructureFeatureMixin {

    /**
     * @reason Return null chunk if biome doesn't match structure
     * @author MrGrim
     */
    @Redirect(method = "locateStructure", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldView;getChunk(IILnet/minecraft/world/chunk/ChunkStatus;)Lnet/minecraft/world/chunk/Chunk;", ordinal = 0),
            slice = @Slice(from = @At(value = "FIELD", opcode = Opcodes.GETSTATIC, target = "Lnet/minecraft/world/chunk/ChunkStatus;STRUCTURE_STARTS:Lnet/minecraft/world/chunk/ChunkStatus;", ordinal = 0),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;getPos()Lnet/minecraft/util/math/ChunkPos;", ordinal = 0)))
    private Chunk biomeConditionalGetChunk(WorldView worldView, int x, int z, ChunkStatus status)
    {
        //magic numbers << 2) + 2 and biomeY = 0 taken from ChunkGenerator.setStructureStarts
        //noinspection rawtypes
        if (worldView.getBiomeForNoiseGen((x << 2) + 2, 0, (z << 2) + 2).getGenerationSettings().hasStructureFeature((StructureFeature) (Object) this)) {
            return worldView.getChunk(x, z, status);
        } else {
            return null;
        }
    }

    /**
     * @reason Can't avoid the call to Chunk.getPos(), and now the chunk might be null.
     * Send a new (0,0) ChunkPos if so. It won't be used anyway.
     * @author MrGrim
     */
    @Redirect(method = "locateStructure", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;getPos()Lnet/minecraft/util/math/ChunkPos;", ordinal = 0),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldView;getChunk(IILnet/minecraft/world/chunk/ChunkStatus;)Lnet/minecraft/world/chunk/Chunk;", ordinal = 0),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/ChunkSectionPos;from(Lnet/minecraft/util/math/ChunkPos;I)Lnet/minecraft/util/math/ChunkSectionPos;", ordinal = 0)))
    private ChunkPos checkForNull(Chunk chunk)
    {
        return chunk == null ? new ChunkPos(0, 0) : chunk.getPos();
    }

    /**
     * @reason Return null here if the chunk is null. This will bypass the following if statement
     * allowing the search to continue.
     * @author MrGrim
     */
    @Redirect(method = "locateStructure", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/StructureAccessor;getStructureStart(Lnet/minecraft/util/math/ChunkSectionPos;Lnet/minecraft/world/gen/feature/StructureFeature;Lnet/minecraft/world/StructureHolder;)Lnet/minecraft/structure/StructureStart;", ordinal = 0),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/ChunkSectionPos;from(Lnet/minecraft/util/math/ChunkPos;I)Lnet/minecraft/util/math/ChunkSectionPos;", ordinal = 0),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/structure/StructureStart;hasChildren()Z", ordinal = 0)))
    private StructureStart<?> checkChunkBeforeGetStructureStart(StructureAccessor structureAccessor, ChunkSectionPos sectionPos, StructureFeature<?> thisStructure, StructureHolder chunk)
    {
        return chunk == null ? null : structureAccessor.getStructureStart(sectionPos, thisStructure, chunk);
    }
}