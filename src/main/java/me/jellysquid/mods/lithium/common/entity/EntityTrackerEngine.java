package me.jellysquid.mods.lithium.common.entity;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.jellysquid.mods.lithium.common.entity.nearby.EntityWithNearbyListener;
import me.jellysquid.mods.lithium.common.entity.nearby.NearbyEntityListener;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.ChunkSectionPos;

import java.util.ArrayList;
import java.util.List;

public class EntityTrackerEngine {
    private final Long2ObjectOpenHashMap<TrackedEntityList> sections = new Long2ObjectOpenHashMap<>();

    public void addEntity(int x, int y, int z, LivingEntity entity) {
        TrackedEntityList list = this.getOrCreateList(x, y, z);
        list.addTrackedEntity(entity);

        if (entity instanceof EntityWithNearbyListener) {
            this.onEntityWithListenerAdded(x, y, z, ((EntityWithNearbyListener) entity).getListener());
        }
    }

    public void removeEntity(int x, int y, int z, LivingEntity entity) {
        TrackedEntityList list = this.getList(x, y, z);

        if (list == null) {
            return;
        }

        if (list.removeTrackedEntity(entity)) {
            if (entity instanceof EntityWithNearbyListener) {
                this.onEntityWithListenerRemoved(x, y, z, ((EntityWithNearbyListener) entity).getListener());
            }
        }
    }

    private void onEntityWithListenerAdded(int x, int y, int z, NearbyEntityListener listener) {
        if (listener.getRange() == 0) {
            return;
        }

        int r = listener.getRange() >> 4;

        for (int x2 = x - r; x2 <= x + r; x2++) {
            for (int y2 = y - r; y2 <= y + r; y2++) {
                for (int z2 = z - r; z2 <= z + r; z2++) {
                    TrackedEntityList list = this.getOrCreateList(x2, y2, z2);
                    list.addListener(listener);
                }
            }
        }
    }

    private void onEntityWithListenerRemoved(int x, int y, int z, NearbyEntityListener listener) {
        if (listener.getRange() == 0) {
            return;
        }

        int r = listener.getRange() >> 4;

        for (int x2 = x - r; x2 <= x + r; x2++) {
            for (int y2 = y - r; y2 <= y + r; y2++) {
                for (int z2 = z - r; z2 <= z + r; z2++) {
                    TrackedEntityList list = this.getList(x2, y2, z2);

                    if (list == null) {
                        throw new IllegalStateException("There is no list of listeners despite being within the range of another listener");
                    }

                    list.removeListener(listener);
                }
            }
        }
    }

    private TrackedEntityList getOrCreateList(int x, int y, int z) {
        return this.sections.computeIfAbsent(encode(x, y, z), TrackedEntityList::new);
    }

    private TrackedEntityList getList(int x, int y, int z) {
        return this.sections.get(encode(x, y, z));
    }

    private static long encode(int x, int y, int z) {
        return ChunkSectionPos.asLong(x, y, z);
    }

    private class TrackedEntityList {
        private final List<LivingEntity> entities = new ArrayList<>();
        private final List<NearbyEntityListener> listeners = new ArrayList<>();

        private final long key;

        private boolean removed = false;

        private TrackedEntityList(long key) {
            this.key = key;
        }

        public boolean isEmpty() {
            return this.entities.isEmpty() && this.listeners.isEmpty();
        }

        public boolean isRemoved() {
            return this.removed;
        }

        public void markRemoved() {
            this.removed = true;
        }

        public void addListener(NearbyEntityListener listener) {
            for (LivingEntity entity : this.entities) {
                listener.onEntityEnteredRange(entity);
            }

            this.listeners.add(listener);
        }

        public void removeListener(NearbyEntityListener listener) {
            if (this.listeners.remove(listener)) {
                for (LivingEntity entity : this.entities) {
                    listener.onEntityLeftRange(entity);
                }

                this.checkEmpty();
            }
        }

        public void addTrackedEntity(LivingEntity entity) {
            for (NearbyEntityListener listener : this.listeners) {
                listener.onEntityEnteredRange(entity);
            }

            this.entities.add(entity);
        }

        public boolean removeTrackedEntity(LivingEntity entity) {
            boolean ret = this.entities.remove(entity);

            if (ret) {
                for (NearbyEntityListener listener : this.listeners) {
                    listener.onEntityLeftRange(entity);
                }

                this.checkEmpty();
            }

            return ret;
        }

        private void checkEmpty() {
            if (!this.isRemoved() && this.isEmpty()) {
                EntityTrackerEngine.this.sections.remove(this.key);

                this.markRemoved();
            }
        }
    }
}
