package me.jellysquid.mods.lithium.mixin.ai.poi.fast_portals;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.spongepowered.asm.mixin.*;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiSection;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.storage.ChunkIOErrorReporter;
import net.minecraft.world.level.chunk.storage.SectionStorage;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;

@Mixin(PoiManager.class)
public abstract class PointOfInterestStorageMixin extends SectionStorage<PoiSection> {

    @Shadow
    @Final
    private LongSet loadedChunks;

    @Unique
    private final LongSet preloadedCenterChunks = new LongOpenHashSet();
    @Unique
    private int preloadRadius = 0;

    public PointOfInterestStorageMixin(SimpleRegionStorage storageAccess, Function<Runnable, Codec<PoiSection>> codecFactory, Function<Runnable, PoiSection> factory, RegistryAccess registryManager, ChunkIOErrorReporter errorHandler, LevelHeightAccessor world) {
        super(storageAccess, codecFactory, factory, registryManager, errorHandler, world);
    }


    /**
     * @author Crec0, 2No2Name
     * @reason Streams in this method cause unnecessary lag. Simply rewriting this to not use streams, we gain
     * considerable performance. Noticeable when large amount of entities are traveling through nether portals.
     * Furthermore, caching whether all surrounding chunks are loaded is more efficient than caching the state
     * of single chunks only.
     */
    @Overwrite
    public void ensureLoadedAndValid(LevelReader worldView, BlockPos pos, int radius) {
        if (this.preloadRadius != radius) {
            //Usually there is only one preload radius per PointOfInterestStorage. Just in case another mod adjusts it dynamically, we avoid
            //assuming its value.
            this.preloadedCenterChunks.clear();
            this.preloadRadius = radius;
        }
        long chunkPos = ChunkPos.asLong(pos);
        if (this.preloadedCenterChunks.contains(chunkPos)) {
            return;
        }
        int chunkX = SectionPos.blockToSectionCoord(pos.getX());
        int chunkZ = SectionPos.blockToSectionCoord(pos.getZ());

        int chunkRadius = Math.floorDiv(radius, 16);
        int maxHeight = this.levelHeightAccessor.getMaxSection() - 1;
        int minHeight = this.levelHeightAccessor.getMinSection();

        for (int x = chunkX - chunkRadius, xMax = chunkX + chunkRadius; x <= xMax; x++) {
            for (int z = chunkZ - chunkRadius, zMax = chunkZ + chunkRadius; z <= zMax; z++) {
                lithium$preloadChunkIfAnySubChunkContainsPOI(worldView, x, z, minHeight, maxHeight);
            }
        }
        this.preloadedCenterChunks.add(chunkPos);
    }

    @Unique
    private void lithium$preloadChunkIfAnySubChunkContainsPOI(LevelReader worldView, int x, int z, int minSubChunk, int maxSubChunk) {
        ChunkPos chunkPos = new ChunkPos(x, z);
        long longChunkPos = chunkPos.toLong();

        if (this.loadedChunks.contains(longChunkPos)) return;

        for (int y = minSubChunk; y <= maxSubChunk; y++) {
            Optional<PoiSection> section = this.getOrLoad(SectionPos.asLong(x, y, z));
            if (section.isPresent()) {
                boolean result = section.get().isValid();
                if (result) {
                    if (this.loadedChunks.add(longChunkPos)) {
                        worldView.getChunk(x, z, ChunkStatus.EMPTY);
                    }
                    break;
                }
            }
        }
    }
}
