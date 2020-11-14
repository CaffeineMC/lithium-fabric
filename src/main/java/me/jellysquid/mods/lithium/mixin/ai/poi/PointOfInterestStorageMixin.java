package me.jellysquid.mods.lithium.mixin.ai.poi;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import me.jellysquid.mods.lithium.common.world.interests.PointOfInterestSetExtended;
import me.jellysquid.mods.lithium.common.world.interests.PointOfInterestStorageExtended;
import me.jellysquid.mods.lithium.common.world.interests.RegionBasedStorageSectionExtended;
import me.jellysquid.mods.lithium.common.world.interests.iterator.NearbyPointOfInterestStream;
import me.jellysquid.mods.lithium.common.world.interests.iterator.SinglePointOfInterestTypeFilter;
import me.jellysquid.mods.lithium.common.world.interests.types.PointOfInterestTypeHelper;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestSet;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.storage.SerializingRegionBasedStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Mixin(PointOfInterestStorage.class)
public abstract class PointOfInterestStorageMixin extends SerializingRegionBasedStorage<PointOfInterestSet>
        implements PointOfInterestStorageExtended {
    public PointOfInterestStorageMixin(File directory, Function<Runnable, Codec<PointOfInterestSet>> function,
                                       Function<Runnable, PointOfInterestSet> function2, DataFixer dataFixer,
                                       DataFixTypes dataFixTypes, boolean bl) {
        super(directory, function, function2, dataFixer, dataFixTypes, bl);
    }

    /**
     * @reason Avoid Stream API
     * @author Jellysquid
     */
    @Overwrite
    public void initForPalette(ChunkPos chunkPos_1, ChunkSection section) {
        ChunkSectionPos sectionPos = ChunkSectionPos.from(chunkPos_1, section.getYOffset() >> 4);

        PointOfInterestSet set = this.get(sectionPos.asLong()).orElse(null);

        if (set != null) {
            set.updatePointsOfInterest((consumer) -> {
                if (PointOfInterestTypeHelper.shouldScan(section)) {
                    this.scanAndPopulate(section, sectionPos, consumer);
                }
            });
        } else {
            if (PointOfInterestTypeHelper.shouldScan(section)) {
                set = this.getOrCreate(sectionPos.asLong());

                this.scanAndPopulate(section, sectionPos, set::add);
            }
        }
    }

    /**
     * @reason Retrieve all points of interest in one operation
     * @author JellySquid
     */
    @SuppressWarnings("unchecked")
    @Overwrite
    public Stream<PointOfInterest> getInChunk(Predicate<PointOfInterestType> predicate, ChunkPos pos,
                                              PointOfInterestStorage.OccupationStatus status) {
        return ((RegionBasedStorageSectionExtended<PointOfInterestSet>) this)
                .getWithinChunkColumn(pos.x, pos.z)
                .flatMap((set) -> set.get(predicate, status));
    }

    /**
     * @reason Retrieve all points of interest in one operation
     * @author JellySquid
     */
    @Overwrite
    public Optional<BlockPos> getPosition(Predicate<PointOfInterestType> typePredicate, Predicate<BlockPos> posPredicate,
                                          PointOfInterestStorage.OccupationStatus status, BlockPos pos, int radius,
                                          Random rand) {
        List<PointOfInterest> list = this.collectWithinRadius(typePredicate, pos, radius, status);

        Collections.shuffle(list, rand);

        for (PointOfInterest point : list) {
            if (posPredicate.test(point.getPos())) {
                return Optional.of(point.getPos());
            }
        }

        return Optional.empty();
    }

    /**
     * @reason Avoid stream-heavy code, use a faster iterator and callback-based approach
     * @author JellySquid
     */
    @Overwrite
    public Optional<BlockPos> getNearestPosition(Predicate<PointOfInterestType> predicate, BlockPos pos, int radius,
                                                 PointOfInterestStorage.OccupationStatus status) {
        List<PointOfInterest> points = this.collectWithinRadius(predicate, pos, radius, status);

        BlockPos nearest = null;
        double nearestDistance = Double.POSITIVE_INFINITY;

        for (PointOfInterest point : points) {
            double distance = point.getPos().getSquaredDistance(pos);

            if (distance < nearestDistance) {
                nearest = point.getPos();
                nearestDistance = distance;
            }
        }

        return Optional.ofNullable(nearest);
    }

    /**
     * @reason Avoid stream-heavy code, use a faster iterator and callback-based approach
     * @author JellySquid
     */
    @Overwrite
    public long count(Predicate<PointOfInterestType> predicate, BlockPos pos, int radius,
                      PointOfInterestStorage.OccupationStatus status) {
        return this.collectWithinRadius(predicate, pos, radius, status).size();
    }

    /**
     * @author JellySquid
     * @reason Avoid stream-heavy code, use faster filtering and fetches
     */
    @Overwrite
    public Stream<PointOfInterest> getInCircle(Predicate<PointOfInterestType> predicate, BlockPos origin, int radius,
                                               PointOfInterestStorage.OccupationStatus status) {
        double radiusSq = radius * radius;

        return this.iterateSpiral(origin, radius, status, predicate, poi -> {
            return isWithinCircleRadius(poi.getPos(), radiusSq, origin);
        });
    }

    @Override
    public Optional<BlockPos> findNearestInSquare(BlockPos origin, int radius, PointOfInterestType type,
                                                  PointOfInterestStorage.OccupationStatus status,
                                                  Predicate<PointOfInterest> predicate) {
        return this.iterateSpiral(origin, radius, status, new SinglePointOfInterestTypeFilter(type), poi -> {
            return isWithinSquareRadius(origin, radius, poi.getPos()) && predicate.test(poi);
        }).map(PointOfInterest::getPos).findFirst();
    }

    private List<PointOfInterest> collectWithinRadius(Predicate<PointOfInterestType> predicate, BlockPos origin,
                                                      int radius, PointOfInterestStorage.OccupationStatus status) {

        double radiusSq = radius * radius;

        int minChunkX = (origin.getX() - radius - 1) >> 4;
        int minChunkZ = (origin.getZ() - radius - 1) >> 4;

        int maxChunkX = (origin.getX() + radius + 1) >> 4;
        int maxChunkZ = (origin.getZ() + radius + 1) >> 4;

        // noinspection unchecked
        RegionBasedStorageSectionExtended<PointOfInterestSet> storage = ((RegionBasedStorageSectionExtended<PointOfInterestSet>) this);

        List<PointOfInterest> points = new ArrayList<>();
        Consumer<PointOfInterest> collector = (point) -> {
            if (isWithinCircleRadius(origin, radiusSq, point.getPos())) {
                points.add(point);
            }
        };

        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                for (PointOfInterestSet set : storage.getInChunkColumn(x, z)) {
                    ((PointOfInterestSetExtended) set).collectMatchingPoints(predicate, status, collector);
                }
            }
        }

        return points;
    }

    private Stream<PointOfInterest> iterateSpiral(BlockPos origin, int radius,
                                                  PointOfInterestStorage.OccupationStatus status,
                                                  Predicate<PointOfInterestType> typePredicate,
                                                  Predicate<PointOfInterest> pointPredicate) {
        // noinspection unchecked
        RegionBasedStorageSectionExtended<PointOfInterestSet> storage = ((RegionBasedStorageSectionExtended<PointOfInterestSet>) this);

        return StreamSupport.stream(new NearbyPointOfInterestStream(typePredicate, status, pointPredicate, origin, radius, storage), false);
    }

    private static boolean isWithinSquareRadius(BlockPos origin, int radius, BlockPos pos) {
        return Math.abs(pos.getX() - origin.getX()) <= radius &&
                Math.abs(pos.getZ() - origin.getZ()) <= radius;
    }

    private static boolean isWithinCircleRadius(BlockPos origin, double radiusSq, BlockPos pos) {
        return origin.getSquaredDistance(pos) <= radiusSq;
    }

    @Shadow
    protected abstract void scanAndPopulate(ChunkSection section, ChunkSectionPos sectionPos, BiConsumer<BlockPos, PointOfInterestType> entryConsumer);
}
