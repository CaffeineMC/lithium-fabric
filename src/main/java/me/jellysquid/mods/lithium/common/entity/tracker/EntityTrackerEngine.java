package me.jellysquid.mods.lithium.common.entity.tracker;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import me.jellysquid.mods.lithium.common.entity.tracker.nearby.NearbyEntityListener;
import me.jellysquid.mods.lithium.common.entity.tracker.nearby.NearbyEntityListenerProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Tracks the entities within a world and provides notifications to listeners when a tracked entity enters or leaves a
 * watched area. This removes the necessity to constantly poll the world for nearby entities each tick and generally
 * provides a sizable boost to performance.
 */
public class EntityTrackerEngine {
    private final Long2ObjectOpenHashMap<TrackedEntityList> sections;
    private final Reference2ReferenceOpenHashMap<NearbyEntityListener, List<TrackedEntityList>> sectionsByEntity;


    public EntityTrackerEngine() {
        this.sections = new Long2ObjectOpenHashMap<>();
        this.sectionsByEntity = new Reference2ReferenceOpenHashMap<>();
    }

    /**
     * Called when an entity is added to the world.
     */
    public void onEntityAdded(int x, int y, int z, LivingEntity entity) {
        if (this.addEntity(x, y, z, entity)) {
            if (entity instanceof NearbyEntityListenerProvider) {
                this.addListener(x, y, z, ((NearbyEntityListenerProvider) entity).getListener());
            }
        }
    }

    /**
     * Called when an entity is removed from the world.
     */
    public void onEntityRemoved(int x, int y, int z, LivingEntity entity) {
        if (this.removeEntity(x, y, z, entity)) {
            if (entity instanceof NearbyEntityListenerProvider) {
                this.removeListener(((NearbyEntityListenerProvider) entity).getListener());
            }
        }
    }

    /**
     * Called when an entity moves between chunks within a world. This is less expensive to call than manually
     * removing/adding an entity from chunks each time it moves.
     */
    public void onEntityMoved(int aX, int aY, int aZ, int bX, int bY, int bZ, LivingEntity entity) {
        if (this.removeEntity(aX, aY, aZ, entity) && this.addEntity(bX, bY, bZ, entity)) {
            if (entity instanceof NearbyEntityListenerProvider) {
                this.moveListener(aX, aY, aZ, bX, bY, bZ, ((NearbyEntityListenerProvider) entity).getListener());
            }
        }
    }

    private boolean addEntity(int x, int y, int z, LivingEntity entity) {
        return this.getOrCreateList(x, y, z).addTrackedEntity(entity);
    }

    private boolean removeEntity(int x, int y, int z, LivingEntity entity) {
        TrackedEntityList list = this.getList(x, y, z);

        if (list == null) {
            return false;
        }

        return list.removeTrackedEntity(entity);
    }

    private void addListener(int x, int y, int z, NearbyEntityListener listener) {
        int r = listener.getChunkRange();

        if (r == 0) {
            return;
        }

        if (this.sectionsByEntity.containsKey(listener)) {
            //todo 1.17 y value fix
            throw new IllegalStateException(errorMessageAlreadyListening(this.sectionsByEntity, listener, ChunkSectionPos.from(x, y, z)));
        }

        int yMin = Math.max(0, y - r);
        int yMax = Math.min(y + r, 15);

        List<TrackedEntityList> all = new ArrayList<>((2 * r + 1) * (yMax - yMin + 1) * (2 * r + 1));

        for (int x2 = x - r; x2 <= x + r; x2++) {
            for (int y2 = yMin; y2 <= yMax; y2++) {
                for (int z2 = z - r; z2 <= z + r; z2++) {
                    TrackedEntityList list = this.getOrCreateList(x2, y2, z2);
                    list.addListener(listener);

                    all.add(list);
                }
            }
        }

        this.sectionsByEntity.put(listener, all);
    }

    private void removeListener(NearbyEntityListener listener) {
        int r = listener.getChunkRange();

        if (r == 0) {
            return;
        }

        List<TrackedEntityList> all = this.sectionsByEntity.remove(listener);

        if (all != null) {
            for (TrackedEntityList list : all) {
                list.removeListener(listener);
            }
        } else {
            throw new IllegalArgumentException("Entity listener not tracked:" + listener.toString());
        }
    }

    // Faster implementation which avoids removing from/adding to every list twice on an entity move event
    private void moveListener(int aX, int aY, int aZ, int bX, int bY, int bZ, NearbyEntityListener listener) {
        int radius = listener.getChunkRange();

        if (radius == 0) {
            return;
        }

        BlockBox before = new BlockBox(aX - radius, aY - radius, aZ - radius, aX + radius, aY + radius, aZ + radius);
        BlockBox after = new BlockBox(aX - radius, aY - radius, aZ - radius, bX + radius, bY + radius, bZ + radius);

        BlockBox merged = new BlockBox(before.minX, before.minY, before.minZ, before.maxX, before.maxY, before.maxZ);
        merged.encompass(after);

        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int x = merged.minX; x <= merged.maxX; x++) {
            for (int y = merged.minY; y <= merged.maxY; y++) {
                for (int z = merged.minZ; z <= merged.maxZ; z++) {
                    pos.set(x, y, z);

                    boolean leaving = before.contains(pos);
                    boolean entering = after.contains(pos);

                    // Nothing to change
                    if (leaving == entering) {
                        continue;
                    }

                    if (leaving) {
                        // The listener has left the chunk
                        TrackedEntityList list = this.getList(x, y, z);

                        if (list == null) {
                            throw new IllegalStateException("Expected there to be a listener list while moving entity but there was none");
                        }

                        list.removeListener(listener);
                    } else {
                        // The listener has entered the chunk
                        TrackedEntityList list = this.getOrCreateList(x, y, z);
                        list.addListener(listener);
                    }
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

    private static ChunkSectionPos decode(long xyz) {
        return ChunkSectionPos.from(xyz);
    }

    private class TrackedEntityList {
        private final Set<LivingEntity> entities = new ReferenceOpenHashSet<>();
        private final Set<NearbyEntityListener> listeners = new ReferenceOpenHashSet<>();

        private final long key;

        private TrackedEntityList(long key) {
            this.key = key;
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

        public boolean addTrackedEntity(LivingEntity entity) {
            for (NearbyEntityListener listener : this.listeners) {
                listener.onEntityEnteredRange(entity);
            }

            return this.entities.add(entity);
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
            if (this.entities.isEmpty() && this.listeners.isEmpty()) {
                EntityTrackerEngine.this.sections.remove(this.key);
            }
        }
    }


    private static String errorMessageAlreadyListening(Reference2ReferenceOpenHashMap<NearbyEntityListener, List<TrackedEntityList>> sectionsByEntity, NearbyEntityListener listener, ChunkSectionPos newLocation) {
        StringBuilder builder = new StringBuilder();
        builder.append("Adding Entity listener a second time: ").append(listener.toString());
        builder.append("\n");
        builder.append(" wants to listen at: ").append(newLocation.toString());
        builder.append(" with cube radius: ").append(listener.getChunkRange());
        builder.append("\n");
        builder.append(" but was already listening at chunk sections: ");
        String[] comma = new String[]{""};
        if (sectionsByEntity.get(listener) == null) {
            builder.append("null");
        } else {
            sectionsByEntity.get(listener).forEach(a -> {
                builder.append(comma[0]);
                builder.append(decode(a.key).toString());
                comma[0] = ", ";
            });
        }
        return builder.toString();
    }
}
