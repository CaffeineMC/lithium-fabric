package me.jellysquid.mods.lithium.mixin.ai.poi;

import com.mojang.serialization.Codec;
import me.jellysquid.mods.lithium.common.util.Distances;
import me.jellysquid.mods.lithium.common.world.interests.PointOfInterestSetExtended;
import me.jellysquid.mods.lithium.common.world.interests.PointOfInterestStorageExtended;
import me.jellysquid.mods.lithium.common.world.interests.RegionBasedStorageSectionExtended;
import me.jellysquid.mods.lithium.common.world.interests.iterator.NearbyPointOfInterestStream;
import me.jellysquid.mods.lithium.common.world.interests.iterator.SinglePointOfInterestTypeFilter;
import me.jellysquid.mods.lithium.common.world.interests.iterator.SphereChunkOrderedPoiSetSpliterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiSection;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.storage.ChunkIOErrorReporter;
import net.minecraft.world.level.chunk.storage.SectionStorage;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Mixin(PoiManager.class)
public abstract class PointOfInterestStorageMixin extends SectionStorage<PoiSection>
        implements PointOfInterestStorageExtended {


    public PointOfInterestStorageMixin(SimpleRegionStorage storageAccess, Function<Runnable, Codec<PoiSection>> codecFactory, Function<Runnable, PoiSection> factory, RegistryAccess registryManager, ChunkIOErrorReporter errorHandler, LevelHeightAccessor world) {
        super(storageAccess, codecFactory, factory, registryManager, errorHandler, world);
    }

    /**
     * @reason Retrieve all points of interest in one operation
     * @author JellySquid
     */
    @VisibleForDebug
    @SuppressWarnings("unchecked")
    @Overwrite
    public Stream<PoiRecord> getInChunk(Predicate<Holder<PoiType>> predicate, ChunkPos pos,
                                              PoiManager.Occupancy status) {
        return ((RegionBasedStorageSectionExtended<PoiSection>) this)
                .lithium$getWithinChunkColumn(pos.x, pos.z)
                .flatMap(set -> set.getRecords(predicate, status));
    }

    /**
     * Gets a random POI that matches the requirements. Uses spherical radius.
     *
     * @reason Retrieve all points of interest in one operation, avoid stream code
     * @author JellySquid
     */
    @Overwrite
    public Optional<BlockPos> getRandom(Predicate<Holder<PoiType>> typePredicate, Predicate<BlockPos> posPredicate,
                                          PoiManager.Occupancy status, BlockPos pos, int radius,
                                          RandomSource rand) {
        ArrayList<PoiRecord> list = this.withinSphereChunkSectionSorted(typePredicate, pos, radius, status);

        for (int i = list.size() - 1; i >= 0; i--) {
            //shuffle by swapping randomly
            PoiRecord currentPOI = list.set(rand.nextInt(i + 1), list.get(i));
            list.set(i, currentPOI); //Move to the end of the unconsumed part of the list

            //consume while shuffling, abort shuffling when result found
            if (posPredicate.test(currentPOI.getPos())) {
                return Optional.of(currentPOI.getPos());
            }
        }

        return Optional.empty();
    }

    /**
     * Gets the closest POI that matches the requirements.
     *
     * @reason Avoid stream-heavy code, use a faster iterator and callback-based approach
     * @author 2No2Name
     */
    @Overwrite
    public Optional<BlockPos> findClosest(Predicate<Holder<PoiType>> predicate, BlockPos pos, int radius,
                                                 PoiManager.Occupancy status) {
        return this.findClosest(predicate, null, pos, radius, status);
    }

    /**
     * Gets the closest POI that matches the requirements.
     * If there are several closest POIs, negative chunk coordinate first (sort by x, then z, then y)
     *
     * @reason Avoid stream-heavy code, use a faster iterator and callback-based approach
     * @author JellySquid, 2No2Name
     */
    @Overwrite
    public Optional<BlockPos> findClosest(Predicate<Holder<PoiType>> predicate,
                                                 Predicate<BlockPos> posPredicate, BlockPos pos, int radius,
                                                 PoiManager.Occupancy status) {
        Stream<PoiRecord> pointOfInterestStream = this.streamOutwards(pos, radius, status, true, false, predicate, posPredicate == null ? null : poi -> posPredicate.test(poi.getPos()));
        return pointOfInterestStream.map(PoiRecord::getPos).findFirst();
    }

    /**
     * Get number of matching POIs in sphere
     *
     * @reason Avoid stream-heavy code, use a faster iterator and callback-based approach
     * @author JellySquid
     */
    @Overwrite
    public long getCountInRange(Predicate<Holder<PoiType>> predicate, BlockPos pos, int radius,
                      PoiManager.Occupancy status) {
        return this.withinSphereChunkSectionSorted(predicate, pos, radius, status).size();
    }

    /**
     * Get all POI in sphere around origin with given radius. Order is vanilla order
     * Vanilla order (might be undefined, but pratically):
     * Chunk section order: Negative X first, if equal, negative Z first, if equal, negative Y first.
     * Within the chunk section: Whatever the internal order is (we are not modifying that)
     *
     * @author JellySquid
     * @reason Avoid stream-heavy code, use faster filtering and fetches
     */
    @Overwrite
    public Stream<PoiRecord> getInRange(Predicate<Holder<PoiType>> predicate, BlockPos sphereOrigin, int radius,
                                               PoiManager.Occupancy status) {
        return this.withinSphereChunkSectionSortedStream(predicate, sphereOrigin, radius, status);
    }

    @Override
    public Optional<PoiRecord> lithium$findNearestForPortalLogic(BlockPos origin, int radius, Holder<PoiType> type,
                                                                       PoiManager.Occupancy status,
                                                                       Predicate<PoiRecord> afterSortPredicate, WorldBorder worldBorder) {
        // Order of the POI:
        // return closest accepted POI (L2 distance). If several exist:
        // return the one with most negative Y. If several exist:
        // return the one with most negative X. If several exist:
        // return the one with most negative Z. If several exist: Be confused about two POIs being in the same location.

        boolean worldBorderIsFarAway = worldBorder == null || worldBorder.getDistanceToBorder(origin.getX(), origin.getZ()) > radius + 3;
        Predicate<PoiRecord> poiPredicateAfterSorting;
        if (worldBorderIsFarAway) {
            poiPredicateAfterSorting = afterSortPredicate;
        } else {
            poiPredicateAfterSorting = poi -> worldBorder.isWithinBounds(poi.getPos()) && afterSortPredicate.test(poi);
        }
        return this.streamOutwards(origin, radius, status, true, true, new SinglePointOfInterestTypeFilter(type), poiPredicateAfterSorting).findFirst();
    }

    private Stream<PoiRecord> withinSphereChunkSectionSortedStream(Predicate<Holder<PoiType>> predicate, BlockPos origin,
                                                                         int radius, PoiManager.Occupancy status) {
        double radiusSq = radius * radius;


        // noinspection unchecked
        RegionBasedStorageSectionExtended<PoiSection> storage = (RegionBasedStorageSectionExtended<PoiSection>) this;


        Stream<Stream<PoiSection>> stream = StreamSupport.stream(new SphereChunkOrderedPoiSetSpliterator(radius, origin, storage), false);

        return stream.flatMap((Stream<PoiSection> setStream) -> setStream.flatMap(
                (PoiSection set) -> set.getRecords(predicate, status)
                        .filter(point -> Distances.isWithinCircleRadius(origin, radiusSq, point.getPos()))
        ));
    }

    private ArrayList<PoiRecord> withinSphereChunkSectionSorted(Predicate<Holder<PoiType>> predicate, BlockPos origin,
                                                                      int radius, PoiManager.Occupancy status) {
        double radiusSq = radius * radius;

        int minChunkX = origin.getX() - radius - 1 >> 4;
        int minChunkZ = origin.getZ() - radius - 1 >> 4;

        int maxChunkX = origin.getX() + radius + 1 >> 4;
        int maxChunkZ = origin.getZ() + radius + 1 >> 4;

        // noinspection unchecked
        RegionBasedStorageSectionExtended<PoiSection> storage = (RegionBasedStorageSectionExtended<PoiSection>) this;

        ArrayList<PoiRecord> points = new ArrayList<>();
        Consumer<PoiRecord> collector = point -> {
            if (Distances.isWithinCircleRadius(origin, radiusSq, point.getPos())) {
                points.add(point);
            }
        };

        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                for (PoiSection set : storage.lithium$getInChunkColumn(x, z)) {
                    ((PointOfInterestSetExtended) set).lithium$collectMatchingPoints(predicate, status, collector);
                }
            }
        }

        return points;
    }

    private Stream<PoiRecord> streamOutwards(BlockPos origin, int radius,
                                                   PoiManager.Occupancy status,
                                                   @SuppressWarnings("SameParameterValue") boolean useSquareDistanceLimit,
                                                   boolean preferNegativeY,
                                                   Predicate<Holder<PoiType>> typePredicate,
                                                   @Nullable Predicate<PoiRecord> afterSortingPredicate) {
        // noinspection unchecked
        RegionBasedStorageSectionExtended<PoiSection> storage = (RegionBasedStorageSectionExtended<PoiSection>) this;

        return StreamSupport.stream(new NearbyPointOfInterestStream(typePredicate, status, useSquareDistanceLimit, preferNegativeY, afterSortingPredicate, origin, radius, storage), false);
    }
}
