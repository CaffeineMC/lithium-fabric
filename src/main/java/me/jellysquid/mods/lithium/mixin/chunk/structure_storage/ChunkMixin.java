package me.jellysquid.mods.lithium.mixin.chunk.structure_storage;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import net.minecraft.structure.StructureStart;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Mixin that mostly removes empty structure starts and references, which in turn speeds up spawning entities and
 * reduces memory usage. {@link ChunkSerializerMixin} makes sure that the empty structures will not be missing from
 * chunks saved on disk, just in case future minecraft versions rely on empty structures being present.
 */
@Mixin(Chunk.class)
public class ChunkMixin {
    @Shadow
    @Final
    private Map<StructureFeature<?>, LongSet> structureReferences;

    /**
     * @author 2No2Name
     * @reason avoid allocation, avoid storing default values that are equivalent to null
     */
    @Overwrite
    public LongSet getStructureReferences(StructureFeature<?> structure) {
        return this.structureReferences.getOrDefault(structure, LongSets.EMPTY_SET);
    }

    /**
     * Avoid storing default values that are equivalent to storing null.
     */
    @Inject(
            method = "setStructureReferences(Ljava/util/Map;)V",
            at = @At("HEAD")
    )
    private void removeEmptyStructureFeatureEntries(Map<StructureFeature<?>, LongSet> structureReferences, CallbackInfo ci) {
        if (structureReferences instanceof HashMap && !structureReferences.isEmpty()) {
            structureReferences.values().removeIf(longs -> longs == null || longs.isEmpty());
        }
    }

    /**
     * Avoid storing default values that are equivalent to storing null.
     */
    @Inject(
            method = "setStructureStarts(Ljava/util/Map;)V",
            at = @At("HEAD")
    )
    private void removeEmptyStructureStartEntries(Map<StructureFeature<?>, StructureStart<?>> structureStarts, CallbackInfo ci) {
        if (structureStarts instanceof HashMap && !structureStarts.isEmpty()) {
            structureStarts.values().removeIf(structureStart -> structureStart == null || structureStart == StructureStart.DEFAULT);
        }
    }

    /**
     * @author 2No2Name
     * @reason avoid allocation
     */
    @Overwrite
    public Map<StructureFeature<?>, LongSet> getStructureReferences() {
        Map<StructureFeature<?>, LongSet> structureReferences = this.structureReferences;
        if (structureReferences.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(structureReferences);
    }
}
