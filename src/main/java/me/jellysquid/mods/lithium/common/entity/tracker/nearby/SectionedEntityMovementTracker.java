package me.jellysquid.mods.lithium.common.entity.tracker.nearby;

import it.unimi.dsi.fastutil.HashCommon;
import me.jellysquid.mods.lithium.common.entity.tracker.EntityTrackerEngine;
import me.jellysquid.mods.lithium.common.entity.tracker.EntityTrackerSection;
import me.jellysquid.mods.lithium.common.util.tuples.WorldSectionBox;
import me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking.ServerEntityManagerAccessor;
import me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking.ServerWorldAccessor;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityTrackingSection;
import net.minecraft.world.entity.SectionedEntityCache;

import java.util.ArrayList;

public abstract class SectionedEntityMovementTracker<E extends EntityLike, S> {
    final WorldSectionBox trackedWorldSections;
    final Class<S> clazz;
    private final int trackedClass;
    ArrayList<EntityTrackingSection<E>> sortedSections;
    boolean[] sectionVisible;
    private int timesRegistered;
    private ArrayList<long[]> sectionChangeCounters;

    private long maxChangeTime;

    public SectionedEntityMovementTracker(WorldSectionBox interactionChunks, Class<S> clazz) {
        this.clazz = clazz;
        this.trackedWorldSections = interactionChunks;
        this.trackedClass = EntityTrackerEngine.MOVEMENT_NOTIFYING_ENTITY_CLASSES.indexOf(clazz);
        assert this.trackedClass != -1;
    }

    @Override
    public int hashCode() {
        return HashCommon.mix(this.trackedWorldSections.hashCode()) ^ HashCommon.mix(this.trackedClass) ^ this.getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == this.getClass() &&
                this.clazz == ((SectionedEntityMovementTracker<?, ?>) obj).clazz &&
                this.trackedWorldSections.equals(((SectionedEntityMovementTracker<?, ?>) obj).trackedWorldSections);
    }

    /**
     * Method to quickly check whether any relevant entities moved inside the relevant entity sections after
     * the last interaction attempt.
     *
     * @param lastCheckedTime time of the last interaction attempt
     * @return whether any relevant entity moved in the tracked area
     */
    public boolean isUnchangedSince(long lastCheckedTime) {
        if (lastCheckedTime <= this.maxChangeTime) {
            return false;
        }
        ArrayList<long[]> sectionChangeCounters = this.sectionChangeCounters;
        int trackedClass = this.trackedClass;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, numCounters = sectionChangeCounters.size(); i < numCounters; i++) {
            // >= instead of > is required here, as changes may occur in the same tick but after the last check
            long sectionChangeTime = sectionChangeCounters.get(i)[trackedClass];
            if (lastCheckedTime <= sectionChangeTime) {
                this.setChanged(sectionChangeTime);
                return false;
            }
        }
        return true;
    }

    public void register(ServerWorld world) {
        assert world == this.trackedWorldSections.world();

        if (this.timesRegistered == 0) {
            //noinspection unchecked
            SectionedEntityCache<E> cache = ((ServerEntityManagerAccessor<E>) ((ServerWorldAccessor) world).getEntityManager()).getCache();

            this.sectionChangeCounters = new ArrayList<>();
            WorldSectionBox trackedSections = this.trackedWorldSections;
            int size = trackedSections.numSections();
            assert size > 0;
            this.sortedSections = new ArrayList<>(size);
            this.sectionVisible = new boolean[size];

            //vanilla iteration order in SectionedEntityCache is xzy
            //WorldSectionBox upper coordinates are exclusive
            for (int x = trackedSections.chunkX1(); x < trackedSections.chunkX2(); x++) {
                for (int z = trackedSections.chunkZ1(); z < trackedSections.chunkZ2(); z++) {
                    for (int y = trackedSections.chunkY1(); y < trackedSections.chunkY2(); y++) {
                        EntityTrackingSection<E> section = cache.getTrackingSection(ChunkSectionPos.asLong(x, y, z));
                        EntityTrackerSection sectionAccess = (EntityTrackerSection) section;
                        this.sortedSections.add(section);
                        sectionAccess.addListener(this);
                    }
                }
            }
            this.setChanged(world.getTime());
        }

        this.timesRegistered++;
    }

    public void unRegister(ServerWorld world) {
        assert world == this.trackedWorldSections.world();
        if (--this.timesRegistered > 0) {
            return;
        }
        assert this.timesRegistered == 0;
        //noinspection unchecked
        SectionedEntityCache<E> cache = ((ServerEntityManagerAccessor<E>) ((ServerWorldAccessor) world).getEntityManager()).getCache();
        MovementTrackerCache storage = (MovementTrackerCache) cache;
        storage.remove(this);

        ArrayList<EntityTrackingSection<E>> sections = this.sortedSections;
        for (int i = sections.size() - 1; i >= 0; i--) {
            EntityTrackingSection<E> section = sections.get(i);
            EntityTrackerSection sectionAccess = (EntityTrackerSection) section;
            sectionAccess.removeListener(cache, this);
        }
        this.setChanged(world.getTime());
    }

    /**
     * Register an entity section to this listener, so this listener can look for changes in the section.
     */
    public void onSectionEnteredRange(EntityTrackerSection section) {
        this.setChanged(this.trackedWorldSections.world().getTime());
        //noinspection SuspiciousMethodCalls
        this.sectionVisible[this.sortedSections.lastIndexOf(section)] = true;
        this.sectionChangeCounters.add(section.getMovementTimestampArray());
    }

    public void onSectionLeftRange(EntityTrackerSection section) {
        this.setChanged(this.trackedWorldSections.world().getTime());
        //noinspection SuspiciousMethodCalls
        this.sectionVisible[this.sortedSections.indexOf(section)] = false;
        this.sectionChangeCounters.remove(section.getMovementTimestampArray());
    }

    /**
     * Method that marks that new entities might have appeared or moved in the tracked chunk sections.
     */
    private void setChanged(long atTime) {
        if (atTime > this.maxChangeTime) {
            this.maxChangeTime = atTime;
        }
    }
}
