package me.jellysquid.mods.lithium.common.entity.tracker.nearby;

import me.jellysquid.mods.lithium.common.util.LongObjObjConsumer;
import net.minecraft.entity.Entity;
import net.minecraft.util.collection.TypeFilterableList;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3i;

/**
 * The main interface used to receive events from the
 * {@link me.jellysquid.mods.lithium.common.entity.tracker.EntityTrackerEngine} of a world.
 */
public interface NearbyEntityListener {
    Vec3i EMPTY_RANGE = new Vec3i(0, 0, 0);
    /**
     * Calls the callbacks for the chunk coordinates that this listener is leaving and entering
     */
    default <S> void forEachChunkInRangeChange(S entityCache, ChunkSectionPos prevCenterPos, ChunkSectionPos newCenterPos, LongObjObjConsumer<NearbyEntityListener, S> enteredRangeConsumer, LongObjObjConsumer<NearbyEntityListener, S> leftRangeConsumer) {
        Vec3i chunkRange = this.getChunkRange();
        if (chunkRange == EMPTY_RANGE) {
            return;
        }
        BlockPos.Mutable pos = new BlockPos.Mutable();

        BlockBox after = newCenterPos == null ? null : new BlockBox(newCenterPos.getX() - chunkRange.getX(), newCenterPos.getY() - chunkRange.getY(), newCenterPos.getZ() - chunkRange.getZ(), newCenterPos.getX() + chunkRange.getX(), newCenterPos.getY() + chunkRange.getY(), newCenterPos.getZ() + chunkRange.getZ());
        BlockBox before = prevCenterPos == null ? null : new BlockBox(prevCenterPos.getX() - chunkRange.getX(), prevCenterPos.getY() - chunkRange.getY(), prevCenterPos.getZ() - chunkRange.getZ(), prevCenterPos.getX() + chunkRange.getX(), prevCenterPos.getY() + chunkRange.getY(), prevCenterPos.getZ() + chunkRange.getZ());
        if (before != null && leftRangeConsumer != null) {
            for (int x = before.getMinX(); x <= before.getMaxX(); x++) {
                for (int y = before.getMinY(); y <= before.getMaxY(); y++) {
                    for (int z = before.getMinZ(); z <= before.getMaxZ(); z++) {
                        if (after == null || !after.contains(pos.set(x, y, z))) {
                            leftRangeConsumer.accept(ChunkSectionPos.asLong(x, y, z), this, entityCache);
                        }
                    }
                }
            }
        }
        if (after != null && enteredRangeConsumer != null) {
            for (int x = after.getMinX(); x <= after.getMaxX(); x++) {
                for (int y = after.getMinY(); y <= after.getMaxY(); y++) {
                    for (int z = after.getMinZ(); z <= after.getMaxZ(); z++) {
                        if (before == null || !before.contains(pos.set(x, y, z))) {
                            enteredRangeConsumer.accept(ChunkSectionPos.asLong(x, y, z), this, entityCache);
                        }
                    }
                }
            }
        }
    }
    Vec3i getChunkRange();

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

    default <T> void onSectionLeftRange(TypeFilterableList<T> collection) {
        for (Entity entity : collection.getAllOfType(this.getEntityClass())) {
            this.onEntityLeftRange(entity);
        }
    }

    /**
     * Method to add all entities in the iteration order of the chunk section. This order is relevant and necessary
     * to keep vanilla parity.
     * @param collection the collection of Entities that entered the range of this listener
     * @param <T> the type of the Entities in the collection
     */
    default <T> void onSectionEnteredRange(TypeFilterableList<T> collection) {
        for (Entity entity : collection.getAllOfType(this.getEntityClass())) {
            this.onEntityEnteredRange(entity);
        }
    }
}
