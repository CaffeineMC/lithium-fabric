package me.jellysquid.mods.lithium.common.entity.tracker.nearby;

import me.jellysquid.mods.lithium.common.entity.tracker.EntityTrackerSection;
import me.jellysquid.mods.lithium.common.util.tuples.Range6Int;
import net.minecraft.entity.Entity;
import net.minecraft.util.collection.TypeFilterableList;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityTrackingSection;
import net.minecraft.world.entity.SectionedEntityCache;

/**
 * The main interface used to receive events from the
 * {@link me.jellysquid.mods.lithium.common.entity.tracker.EntityTrackerEngine} of a world.
 */
public interface NearbyEntityListener {
    Range6Int EMPTY_RANGE = new Range6Int(0, 0, 0, -1, -1, -1);
    /**
     * Calls the callbacks for the chunk coordinates that this listener is leaving and entering
     */
    default void forEachChunkInRangeChange(SectionedEntityCache<? extends EntityLike> entityCache, ChunkSectionPos prevCenterPos, ChunkSectionPos newCenterPos) {
        Range6Int chunkRange = this.getChunkRange();
        if (chunkRange == EMPTY_RANGE) {
            return;
        }
        BlockPos.Mutable pos = new BlockPos.Mutable();

        BlockBox after = newCenterPos == null ? null : new BlockBox(newCenterPos.getX() - chunkRange.negativeX(), newCenterPos.getY() - chunkRange.negativeY(), newCenterPos.getZ() - chunkRange.negativeZ(), newCenterPos.getX() + chunkRange.positiveX(), newCenterPos.getY() + chunkRange.positiveY(), newCenterPos.getZ() + chunkRange.positiveZ());
        BlockBox before = prevCenterPos == null ? null : new BlockBox(prevCenterPos.getX() - chunkRange.negativeX(), prevCenterPos.getY() - chunkRange.negativeY(), prevCenterPos.getZ() - chunkRange.negativeZ(), prevCenterPos.getX() + chunkRange.positiveX(), prevCenterPos.getY() + chunkRange.positiveY(), prevCenterPos.getZ() + chunkRange.positiveZ());
        if (before != null) {
            for (int x = before.getMinX(); x <= before.getMaxX(); x++) {
                for (int y = before.getMinY(); y <= before.getMaxY(); y++) {
                    for (int z = before.getMinZ(); z <= before.getMaxZ(); z++) {
                        if (after == null || !after.contains(pos.set(x, y, z))) {
                            long sectionPos = ChunkSectionPos.asLong(x, y, z);
                            EntityTrackingSection<? extends EntityLike> trackingSection = entityCache.getTrackingSection(sectionPos);
                            ((EntityTrackerSection) trackingSection).removeListener(entityCache, this);
                            if (trackingSection.isEmpty()) {
                                entityCache.removeSection(sectionPos);
                            }
                        }
                    }
                }
            }
        }
        if (after != null) {
            for (int x = after.getMinX(); x <= after.getMaxX(); x++) {
                for (int y = after.getMinY(); y <= after.getMaxY(); y++) {
                    for (int z = after.getMinZ(); z <= after.getMaxZ(); z++) {
                        if (before == null || !before.contains(pos.set(x, y, z))) {
                            ((EntityTrackerSection) entityCache.getTrackingSection(ChunkSectionPos.asLong(x, y, z))).addListener(this);
                        }
                    }
                }
            }
        }
    }
    Range6Int getChunkRange();

    /**
     * Called by the entity tracker when an entity enters the range of this listener.
     */
    void onEntityEnteredRange(Entity entity);

    /**
     * Called by the entity tracker when an entity leaves the range of this listener or is removed from the world.
     */
    void onEntityLeftRange(Entity entity);

    default Class<? extends Entity> getEntityClass() {
        return Entity.class;
    }

    /**
     * Method to add all entities in the iteration order of the chunk section. This order is relevant and necessary
     * to keep vanilla parity.
     * @param <T> the type of the Entities in the collection
     * @param entityTrackingSection the section the entities are in
     * @param collection the collection of Entities that entered the range of this listener
     */
    default <T> void onSectionEnteredRange(Object entityTrackingSection, TypeFilterableList<T> collection) {
        for (Entity entity : collection.getAllOfType(this.getEntityClass())) {
            this.onEntityEnteredRange(entity);
        }
    }

    default <T> void onSectionLeftRange(Object entityTrackingSection, TypeFilterableList<T> collection) {
        for (Entity entity : collection.getAllOfType(this.getEntityClass())) {
            this.onEntityLeftRange(entity);
        }
    }

}
