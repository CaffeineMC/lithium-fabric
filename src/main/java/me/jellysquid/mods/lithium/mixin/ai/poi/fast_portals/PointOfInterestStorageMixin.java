package me.jellysquid.mods.lithium.mixin.ai.poi.fast_portals;

import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.poi.PointOfInterestSet;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.storage.SerializingRegionBasedStorage;
import org.spongepowered.asm.mixin.*;

import java.nio.file.Path;

@Mixin(PointOfInterestStorage.class)
public abstract class PointOfInterestStorageMixin extends SerializingRegionBasedStorage<PointOfInterestSet> {

    @Shadow
    @Final
    private LongSet preloadedChunks;

    public PointOfInterestStorageMixin(
            Path path, DataFixer dataFixer, boolean dsync,
            DynamicRegistryManager registryManager, HeightLimitView world
    ) {
        super(
                path, PointOfInterestSet::createCodec, PointOfInterestSet::new,
                dataFixer, DataFixTypes.POI_CHUNK, dsync, registryManager, world
        );
    }

    /**
     * @author Crec0
     * @reason Streams in this method cause unnecessary lag. Simply rewriting this to not use streams, we gain
     * considerable performance. Noticeable when large amount of entities are traveling through nether portals.
     */
    @Overwrite
    public void preloadChunks(WorldView worldView, BlockPos pos, int radius) {
        var chunkPos = new ChunkPos(pos);
        var chunkRadius = Math.floorDiv(radius, 16);
        var maxHeight = this.world.getTopSectionCoord() - 1;
        var minHeight = this.world.getBottomSectionCoord();

        for (int x = chunkPos.x - chunkRadius, xMax = chunkPos.x + chunkRadius; x <= xMax; x++) {
            for (int z = chunkPos.z - chunkRadius, zMax = chunkPos.z + chunkRadius; z <= zMax; z++) {
                lithium$preloadChunkIfAnySubChunkContainsPOI(worldView, x, z, minHeight, maxHeight);
            }
        }
    }

    @Unique
    private void lithium$preloadChunkIfAnySubChunkContainsPOI(WorldView worldView, int x, int z, int minSubChunk, int maxSubChunk) {
        for (int y = minSubChunk; y < maxSubChunk; y++) {
            var sectionPos = ChunkSectionPos.from(x, y, z);
            var section = this.get(sectionPos.asLong());
            if (section.map(PointOfInterestSet::isValid).orElse(false)) {
                var chunk = sectionPos.toChunkPos();
                if (this.preloadedChunks.add(chunk.toLong())) {
                    worldView.getChunk(x, z, ChunkStatus.EMPTY);
                }
                break;
            }
        }
    }
}
