package me.jellysquid.mods.lithium.mixin.chunk.structure_storage;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(ChunkSerializer.class)
public class ChunkSerializerMixin {
    private static final Object2ObjectOpenHashMap<StructureFeature<?>, StructureStart<?>> DEFAULT_STRUCTURE_STARTS;
    private static final Map<StructureFeature<?>, StructureStart<?>> DEFAULT_STRUCTURE_STARTS_READONLY;
    private static final Object2ObjectOpenHashMap<StructureFeature<?>, LongSet> DEFAULT_STRUCTURE_REFERENCES;
    private static final Map<StructureFeature<?>, LongSet> DEFAULT_STRUCTURE_REFERENCES_READONLY;

    static {
        Object2ObjectOpenHashMap<StructureFeature<?>, StructureStart<?>> structureStarts = new Object2ObjectOpenHashMap<>();
        Object2ObjectOpenHashMap<StructureFeature<?>, LongSet> structureReferences = new Object2ObjectOpenHashMap<>();
        for (StructureFeature<?> structureFeature : Registry.STRUCTURE_FEATURE) {
            structureStarts.put(structureFeature, StructureStart.DEFAULT);
            structureReferences.put(structureFeature, LongSets.emptySet());
        }
        structureStarts.trim();
        structureReferences.trim();
        DEFAULT_STRUCTURE_STARTS = structureStarts;
        DEFAULT_STRUCTURE_REFERENCES = structureReferences;
        DEFAULT_STRUCTURE_STARTS_READONLY = Object2ObjectMaps.unmodifiable(structureStarts);
        DEFAULT_STRUCTURE_REFERENCES_READONLY = Object2ObjectMaps.unmodifiable(structureReferences);
    }

    @Redirect(
            method = "serialize",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/Chunk;getStructureStarts()Ljava/util/Map;"
            )
    )
    private static Map<StructureFeature<?>, StructureStart<?>> getCompleteStructureStarts(Chunk chunk) {
        Map<StructureFeature<?>, StructureStart<?>> structureStarts = chunk.getStructureStarts();
        if (structureStarts.isEmpty()) {
            return DEFAULT_STRUCTURE_STARTS_READONLY;
        }
        Object2ObjectOpenHashMap<StructureFeature<?>, StructureStart<?>> completeStructureStarts = DEFAULT_STRUCTURE_STARTS.clone();
        completeStructureStarts.putAll(structureStarts);
        return completeStructureStarts;
    }

    @Redirect(
            method = "serialize",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/Chunk;getStructureReferences()Ljava/util/Map;"
            )
    )
    private static Map<StructureFeature<?>, LongSet> getCompleteStructureReferences(Chunk chunk) {
        Map<StructureFeature<?>, LongSet> structureReferences = chunk.getStructureReferences();
        if (structureReferences.isEmpty()) {
            return DEFAULT_STRUCTURE_REFERENCES_READONLY;
        }
        Object2ObjectOpenHashMap<StructureFeature<?>, LongSet> completeStructureReferences = DEFAULT_STRUCTURE_REFERENCES.clone();
        completeStructureReferences.putAll(structureReferences);
        return completeStructureReferences;
    }
}
