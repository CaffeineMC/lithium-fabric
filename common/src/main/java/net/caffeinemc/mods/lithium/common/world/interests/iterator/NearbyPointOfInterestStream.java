
package net.caffeinemc.mods.lithium.common.world.interests.iterator;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.caffeinemc.mods.lithium.common.util.Distances;
import net.caffeinemc.mods.lithium.common.util.tuples.SortedPointOfInterest;
import net.caffeinemc.mods.lithium.common.world.interests.PointOfInterestSetExtended;
import net.caffeinemc.mods.lithium.common.world.interests.RegionBasedStorageSectionExtended;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiSection;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A specialized spliterator which returns points of interests fom the center of the search radius, outwards. This can
 * provide a huge reduction in time for situations where an entity needs to search for a point of interest around a
 * location in the world. For example, nether portals ordinarily search a huge volume around the "expected" location
 * of a portal, but it is almost always right at or nearby to the search origin.
 */
public class NearbyPointOfInterestStream extends Spliterators.AbstractSpliterator<PoiRecord> {
    private final RegionBasedStorageSectionExtended<PoiSection> storage;

    private final Predicate<Holder<PoiType>> typeSelector;
    private final PoiManager.Occupancy occupationStatus;

    private final LongArrayList chunksSortedByMinDistance;
    private final ArrayList<SortedPointOfInterest> points;
    private final Predicate<PoiRecord> afterSortingPredicate;
    private final Consumer<PoiRecord> collector;
    private final BlockPos origin;

    private int chunkIndex;
    private double currChunkMinDistanceSq;
    private int pointIndex;
    private final Comparator<? super SortedPointOfInterest> pointComparator;

    public NearbyPointOfInterestStream(Predicate<Holder<PoiType>> typeSelector,
                                       PoiManager.Occupancy status,
                                       boolean useSquareDistanceLimit,
                                       boolean preferNegativeY,
                                       @Nullable Predicate<PoiRecord> afterSortingPredicate,
                                       BlockPos origin, int radius,
                                       RegionBasedStorageSectionExtended<PoiSection> storage) {
        super(Long.MAX_VALUE, Spliterator.ORDERED);

        this.storage = storage;

        this.chunkIndex = 0;
        this.pointIndex = 0;

        this.points = new ArrayList<>();
        this.occupationStatus = status;
        this.typeSelector = typeSelector;

        this.origin = origin;
        if (useSquareDistanceLimit) {
            this.collector = (point) -> {
                if (Distances.isWithinSquareRadius(this.origin, radius, point.getPos())) {
                    this.points.add(new SortedPointOfInterest(point, this.origin));
                }
            };
        } else {
            double radiusSq = radius * radius;
            this.collector = (point) -> {
                if (Distances.isWithinCircleRadius(this.origin, radiusSq, point.getPos())) {
                    this.points.add(new SortedPointOfInterest(point, this.origin));
                }
            };
        }

        double distanceLimitL2Sq = useSquareDistanceLimit ? radius * radius * 2 : radius * radius;
        this.chunksSortedByMinDistance = initChunkPositions(origin, radius, distanceLimitL2Sq);
        this.afterSortingPredicate = afterSortingPredicate;
        this.pointComparator = preferNegativeY ? (o1, o2) -> {
            // Use the cached values from earlier
            int cmp = Double.compare(o1.distanceSq(), o2.distanceSq());

            if (cmp != 0) {
                return cmp;
            }

            // Sort by the y-coord (bottom-most first) if any points share an identical distance from one another
            int negativeY = Integer.compare(o1.getY(), o2.getY());
            if (negativeY != 0) {
                return negativeY;
            }

            // Sort by the chunk coord
            int cmp3 = Integer.compare(SectionPos.blockToSectionCoord(o1.getX()), SectionPos.blockToSectionCoord(o2.getX()));
            if (cmp3 != 0) {
                return cmp3;
            }
            return Integer.compare(SectionPos.blockToSectionCoord(o1.getZ()), SectionPos.blockToSectionCoord(o2.getZ()));

        } : (o1, o2) -> {
            // Use the cached values from earlier
            int cmp = Double.compare(o1.distanceSq(), o2.distanceSq());

            if (cmp != 0) {
                return cmp;
            }

            // Sort by the chunk coord
            int cmp2 = Integer.compare(SectionPos.blockToSectionCoord(o1.getX()), SectionPos.blockToSectionCoord(o2.getX()));
            if (cmp2 != 0) {
                return cmp2;
            }
            int cmp3 = Integer.compare(SectionPos.blockToSectionCoord(o1.getZ()), SectionPos.blockToSectionCoord(o2.getZ()));
            if (cmp3 != 0) {
                return cmp3;
            }
            return Integer.compare(SectionPos.blockToSectionCoord(o1.getY()), SectionPos.blockToSectionCoord(o2.getY()));

        };
    }

    private static LongArrayList initChunkPositions(BlockPos origin, int radius, double distanceLimitL2Sq) {
        int minChunkX = (origin.getX() - radius - 1) >> 4;
        int minChunkZ = (origin.getZ() - radius - 1) >> 4;

        int maxChunkX = (origin.getX() + radius + 1) >> 4;
        int maxChunkZ = (origin.getZ() + radius + 1) >> 4;

        LongArrayList chunkPositions = new LongArrayList();

        // TODO: Find a better way to go about this that doesn't require allocating a ton of positions
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (distanceLimitL2Sq >= Distances.getMinChunkToBlockDistanceL2Sq(origin, chunkX, chunkZ)) {
                    chunkPositions.add(ChunkPos.asLong(chunkX, chunkZ));
                }
            }
        }

        // Sort all the chunks by their distance to the search origin. The points in each chunk are sorted independently
        // in their own bucket (the chunk itself), but this does not change the ordering of points between each bucket.
        chunkPositions.sort(
                (long c1, long c2) -> Double.compare(
                        Distances.getMinChunkToBlockDistanceL2Sq(origin, ChunkPos.getX(c1), ChunkPos.getZ(c1)),
                        Distances.getMinChunkToBlockDistanceL2Sq(origin, ChunkPos.getX(c2), ChunkPos.getZ(c2))));

        return chunkPositions;
    }


    @Override
    public boolean tryAdvance(Consumer<? super PoiRecord> action) {
        // Accepted POI:

        // Order of the POI:
        // return closest accepted POI (L2 distance). If several exist:
        // return the one with most negative Y. If several exist:
        // return the one with most negative X. If several exist:
        // return the one with most negative Z. If several exist: Be confused about two POIs being in the same location.

        // Check to see if we still have points to return
        if (this.pointIndex < this.points.size()) {
            if (this.tryAdvancePoint(action)) {
                return true;
            }
        }

        // Find the next ordered chunk to scan for points
        while (this.chunkIndex < this.chunksSortedByMinDistance.size()) {
            long chunkPos = this.chunksSortedByMinDistance.getLong(this.chunkIndex);
            int chunkPosX = ChunkPos.getX(chunkPos);
            int chunkPosZ = ChunkPos.getZ(chunkPos);

            // Keep track of the guaranteed minimum distance of newly collected POI. We can only consume the closest
            // POI if we know that there will not be any other POI with a smaller distance.
            this.currChunkMinDistanceSq = Distances.getMinChunkToBlockDistanceL2Sq(this.origin, chunkPosX, chunkPosZ);
            this.chunkIndex++;
            if (this.chunkIndex == this.chunksSortedByMinDistance.size()) {
                this.currChunkMinDistanceSq = Double.POSITIVE_INFINITY;
            }

            int previousSize = this.points.size();
            // Collect all points in the chunk into the active list of points
            for (PoiSection set : this.storage.lithium$getInChunkColumn(chunkPosX, chunkPosZ)) {
                ((PointOfInterestSetExtended) set).lithium$collectMatchingPoints(this.typeSelector, this.occupationStatus, this.collector);
            }

            // If no points were found in this chunk, skip it early and move on
            if (this.points.size() == previousSize) {
                continue;
            }

            this.points.subList(this.pointIndex, this.points.size()).sort(this.pointComparator);

            // Return the first point in the chunk
            if (this.tryAdvancePoint(action)) {
                return true; //Returns true when progress was made by consuming an element
            }
        }

        // Return the first valid point
        return this.tryAdvancePoint(action);
        //Returns true when progress was made by consuming an element
        //Returns false when no progress was made because no more elements exist.
    }


    private boolean tryAdvancePoint(Consumer<? super PoiRecord> action) {
        while (this.pointIndex < this.points.size()) {
            SortedPointOfInterest next = this.points.get(this.pointIndex);

            //Only consume points if we are sure that there are no closer (or same distance) points to be scanned. Otherwise scan more chunks
            if (next.distanceSq() >= this.currChunkMinDistanceSq) {
                return false;
            }
            this.pointIndex++;

            if (this.afterSortingPredicate == null || this.afterSortingPredicate.test(next.poi())) {
                action.accept(next.poi());
                return true; //Progress was made
            }
            //Continue with the other points when condition is not met
        }
        return false; //No more points. Scan more chunks
    }

}