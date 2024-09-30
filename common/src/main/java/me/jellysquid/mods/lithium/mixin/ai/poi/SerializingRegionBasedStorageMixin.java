package me.jellysquid.mods.lithium.mixin.ai.poi;

import com.google.common.collect.AbstractIterator;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.jellysquid.mods.lithium.common.util.Pos;
import me.jellysquid.mods.lithium.common.util.collections.ListeningLong2ObjectOpenHashMap;
import me.jellysquid.mods.lithium.common.world.interests.RegionBasedStorageSectionExtended;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.storage.ChunkIOErrorReporter;
import net.minecraft.world.level.chunk.storage.SectionStorage;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType") // We don't get a choice, this is Minecraft's doing!
@Mixin(SectionStorage.class)
public abstract class SerializingRegionBasedStorageMixin<R> implements RegionBasedStorageSectionExtended<R> {
    @Mutable
    @Shadow
    @Final
    private Long2ObjectMap<Optional<R>> storage;

    @Shadow
    protected abstract Optional<R> get(long pos);

    @Shadow
    protected abstract void readColumn(ChunkPos pos);

    @Shadow
    @Final
    protected LevelHeightAccessor levelHeightAccessor;
    private Long2ObjectOpenHashMap<BitSet> columns;

    @SuppressWarnings("rawtypes")
    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(SimpleRegionStorage storageAccess, Function codecFactory, Function factory, RegistryAccess registryManager, ChunkIOErrorReporter errorHandler, LevelHeightAccessor world, CallbackInfo ci) {
        this.columns = new Long2ObjectOpenHashMap<>();
        this.storage = new ListeningLong2ObjectOpenHashMap<>(this::onEntryAdded, this::onEntryRemoved);
    }

    private void onEntryRemoved(long key, Optional<R> value) {
        int y = Pos.SectionYIndex.fromSectionCoord(this.levelHeightAccessor, SectionPos.y(key));

        // We only care about items belonging to a valid sub-chunk
        if (y < 0 || y >= Pos.SectionYIndex.getNumYSections(this.levelHeightAccessor)) {
            return;
        }

        int x = SectionPos.x(key);
        int z = SectionPos.z(key);

        long pos = ChunkPos.asLong(x, z);
        BitSet flags = this.columns.get(pos);

        if (flags != null) {
            flags.clear(y);
            if (flags.isEmpty()) {
                this.columns.remove(pos);
            }
        }
    }

    private void onEntryAdded(long key, Optional<R> value) {
        int y = Pos.SectionYIndex.fromSectionCoord(this.levelHeightAccessor, SectionPos.y(key));

        // We only care about items belonging to a valid sub-chunk
        if (y < 0 || y >= Pos.SectionYIndex.getNumYSections(this.levelHeightAccessor)) {
            return;
        }

        int x = SectionPos.x(key);
        int z = SectionPos.z(key);

        long pos = ChunkPos.asLong(x, z);

        BitSet flags = this.columns.get(pos);

        if (flags == null) {
            this.columns.put(pos, flags = new BitSet(Pos.SectionYIndex.getNumYSections(this.levelHeightAccessor)));
        }

        flags.set(y, value.isPresent());
    }

    @Override
    public Stream<R> lithium$getWithinChunkColumn(int chunkX, int chunkZ) {
        BitSet sectionsWithPOI = this.getNonEmptyPOISections(chunkX, chunkZ);

        // No items are present in this column
        if (sectionsWithPOI.isEmpty()) {
            return Stream.empty();
        }

        List<R> list = new ArrayList<>();
        int minYSection = Pos.SectionYCoord.getMinYSection(this.levelHeightAccessor);
        for (int chunkYIndex = sectionsWithPOI.nextSetBit(0); chunkYIndex != -1; chunkYIndex = sectionsWithPOI.nextSetBit(chunkYIndex + 1)) {
            int chunkY = chunkYIndex + minYSection;
            //noinspection SimplifyOptionalCallChains
            R r = this.storage.get(SectionPos.asLong(chunkX, chunkY, chunkZ)).orElse(null);
            if (r != null) {
                list.add(r);
            }
        }

        return list.stream();
    }

    @Override
    public Iterable<R> lithium$getInChunkColumn(int chunkX, int chunkZ) {
        BitSet sectionsWithPOI = this.getNonEmptyPOISections(chunkX, chunkZ);

        // No items are present in this column
        if (sectionsWithPOI.isEmpty()) {
            return Collections::emptyIterator;
        }

        Long2ObjectMap<Optional<R>> loadedElements = this.storage;
        LevelHeightAccessor world = this.levelHeightAccessor;

        return () -> new AbstractIterator<>() {
            private int nextBit = sectionsWithPOI.nextSetBit(0);


            @Override
            protected R computeNext() {
                // If the next bit is <0, that means that no remaining set bits exist
                while (this.nextBit >= 0) {
                    Optional<R> next = loadedElements.get(SectionPos.asLong(chunkX, Pos.SectionYCoord.fromSectionIndex(world, this.nextBit), chunkZ));

                    // Find and advance to the next set bit
                    this.nextBit = sectionsWithPOI.nextSetBit(this.nextBit + 1);

                    if (next.isPresent()) {
                        return next.get();
                    }
                }

                return this.endOfData();
            }
        };
    }

    private BitSet getNonEmptyPOISections(int chunkX, int chunkZ) {
        long pos = ChunkPos.asLong(chunkX, chunkZ);

        BitSet flags = this.getNonEmptySections(pos, false);

        if (flags != null) {
            return flags;
        }

        this.readColumn(new ChunkPos(pos));

        return this.getNonEmptySections(pos, true);
    }

    private BitSet getNonEmptySections(long pos, boolean required) {
        BitSet set = this.columns.get(pos);

        if (set == null && required) {
            throw new NullPointerException("No data is present for column: " + new ChunkPos(pos));
        }

        return set;
    }
}
