
package me.jellysquid.mods.lithium.common.world.interests.iterator;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.jellysquid.mods.lithium.common.util.tuples.SortedPointOfInterest;
import me.jellysquid.mods.lithium.common.world.interests.PointOfInterestSetExtended;
import me.jellysquid.mods.lithium.common.world.interests.RegionBasedStorageSectionExtended;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestSet;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A specialized spliterator which returns points of interests fom the center of the search radius, outwards. This can
 * provide a huge reduction in time for situations where an entity needs to search for a point of interest around a
 * location in the world. For example, nether portals ordinarily search a huge volume around the "expected" location
 * of a portal, but it is almost always right at or nearby to the search origin.
 */
public class NearbyPointOfInterestStream extends Spliterators.AbstractSpliterator<PointOfInterest> {
    private final RegionBasedStorageSectionExtended<PointOfInterestSet> storage;

    private final Predicate<PointOfInterestType> typeSelector;
    private final PointOfInterestStorage.OccupationStatus occupationStatus;

    private final List<ChunkPos> chunks;
    private final List<SortedPointOfInterest> points;
    private final Consumer<PointOfInterest> collector;

    private int chunkIndex;
    private int pointIndex;

    public NearbyPointOfInterestStream(Predicate<PointOfInterestType> typeSelector,
                                       PointOfInterestStorage.OccupationStatus status,
                                       Predicate<PointOfInterest> predicate,
                                       BlockPos origin, int radius,
                                       RegionBasedStorageSectionExtended<PointOfInterestSet> storage) {
        super(Long.MAX_VALUE, Spliterator.ORDERED);

        this.storage = storage;

        this.chunkIndex = 0;
        this.pointIndex = 0;

        this.points = new ArrayList<>();
        this.occupationStatus = status;
        this.typeSelector = typeSelector;

        this.collector = (point) -> {
            if (predicate.test(point)) {
                this.points.add(new SortedPointOfInterest(point, origin));
            }
        };

        this.chunks = initChunkPositions(origin, radius);
    }

    private static List<ChunkPos> initChunkPositions(BlockPos origin, int radius) {
        ChunkPos originChunk = new ChunkPos(origin);

        int minChunkX = (origin.getX() - radius - 1) >> 4;
        int minChunkZ = (origin.getZ() - radius - 1) >> 4;

        int maxChunkX = (origin.getX() + radius + 1) >> 4;
        int maxChunkZ = (origin.getZ() + radius + 1) >> 4;

        List<ChunkPos> list = new ObjectArrayList<>();

        // TODO: Find a better way to go about this that doesn't require allocating a ton of positions
        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                list.add(new ChunkPos(x, z));
            }
        }

        // Sort all the chunks by their distance to the search origin. The points in each chunk are sorted independently
        // in their own bucket (the chunk itself), but this does not change the ordering of points between each bucket.
        list.sort(Comparator.comparingDouble(originChunk::method_24022));

        return list;
    }

    @Override
    public boolean tryAdvance(Consumer<? super PointOfInterest> action) {
        // Check to see if we still have points to return
        if (this.pointIndex < this.points.size()) {
            return this.tryAdvancePoint(action);
        }

        // Find the next ordered chunk to scan for points
        while (this.chunkIndex < this.chunks.size()) {
            ChunkPos chunkPos = this.chunks.get(this.chunkIndex);
            this.chunkIndex++;

            // Reset the list of sorted points
            this.points.clear();
            this.pointIndex = 0;

            // Collect all points in the chunk into the active list of points
            for (PointOfInterestSet set : this.storage.getInChunkColumn(chunkPos.x, chunkPos.z)) {
                ((PointOfInterestSetExtended) set).collectMatchingPoints(this.typeSelector, this.occupationStatus, this.collector);
            }

            // If no points were found in this chunk, skip it early and move on
            if (this.points.isEmpty()) {
                continue;
            }

            // Sort the points in the chunk by their distance to the origin, nearest first.
            // Despite IDEA's advice, using comparator chaining results in drastically slower code due to all the
            // method indirection the compiler has to deal with. We simply implement the (mostly trivial) logic in
            // our own comparator below.

            // noinspection ComparatorCombinators
            this.points.sort((o1, o2) -> {
                // Use the cached values from earlier
                int dist = Double.compare(o1.distance, o2.distance);

                if (dist != 0) {
                    return dist;
                }

                // Sort by the y-coord (bottom-most first) if any points share an identical distance from one another
                return Integer.compare(o1.getY(), o2.getY());
            });

            // Return the first point in the chunk
            return this.tryAdvancePoint(action);
        }

        return false;
    }

    private boolean tryAdvancePoint(Consumer<? super PointOfInterest> action) {
        SortedPointOfInterest next = this.points.get(this.pointIndex++);
        action.accept(next.poi);

        // Returns true if this stream has any potential remaining elements
        return this.pointIndex < this.points.size() ||
                this.chunkIndex < this.chunks.size();
    }

}