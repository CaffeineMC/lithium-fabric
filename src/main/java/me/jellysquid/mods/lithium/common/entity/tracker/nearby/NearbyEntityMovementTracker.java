package me.jellysquid.mods.lithium.common.entity.tracker.nearby;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import me.jellysquid.mods.lithium.common.entity.tracker.EntityTrackerEngine;
import me.jellysquid.mods.lithium.common.entity.tracker.EntityTrackerSection;
import me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking.ServerEntityManagerAccessor;
import me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking.ServerWorldAccessor;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityTrackingSection;
import net.minecraft.world.entity.SectionedEntityCache;

import java.util.ArrayList;
import java.util.List;

public abstract class NearbyEntityMovementTracker<E extends EntityLike, S> {
    final Box enclosingBox;
    ArrayList<EntityTrackingSection<E>> sortedSections;
    boolean[] sectionVisible;
    final Class<S> clazz;
    private final int trackedClass;

    private long lastCheckedTime = Long.MIN_VALUE;

    public NearbyEntityMovementTracker(Box interactionArea, Class<S> clazz) {
        this.clazz = clazz;
        this.enclosingBox = interactionArea;
        this.trackedClass = EntityTrackerEngine.MOVEMENT_NOTIFYING_ENTITY_CLASSES.indexOf(clazz);
        assert this.trackedClass != -1;
    }

    /**
     * Method to quickly check whether any relevant entities moved inside the relevant entity sections after
     * the last {@link NearbyEntityMovementTracker#setUnchanged(long)}
     * @return whether any relevant entity moved in the tracked area
     */
    public boolean isUnchanged() {
        return this.lastCheckedTime != Long.MIN_VALUE && !checkForChanges();
    }

    public void register(ServerWorld world) {
        this.updateRegistration(world, true);
    }

    public void unRegister(ServerWorld world) {
        this.updateRegistration(world, false);
    }

    private void updateRegistration(ServerWorld world, boolean register) {
        //noinspection unchecked
        SectionedEntityCache<E> cache = ((ServerEntityManagerAccessor<E>) ((ServerWorldAccessor) world).getEntityManager()).getCache();

        ArrayList<EntityTrackingSection<E>> sortedSections = register ? new ArrayList<>() : null;
        BooleanArrayList sectionVisible = register ? new BooleanArrayList() : null;
        int minX = ChunkSectionPos.getSectionCoord(this.enclosingBox.minX - 2.0D);
        int minY = ChunkSectionPos.getSectionCoord(this.enclosingBox.minY - 2.0D);
        int minZ = ChunkSectionPos.getSectionCoord(this.enclosingBox.minZ - 2.0D);
        int maxX = ChunkSectionPos.getSectionCoord(this.enclosingBox.maxX + 2.0D);
        int maxY = ChunkSectionPos.getSectionCoord(this.enclosingBox.maxY + 2.0D);
        int maxZ = ChunkSectionPos.getSectionCoord(this.enclosingBox.maxZ + 2.0D);
        //vanilla iteration order in SectionedEntityCache is xzy
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    EntityTrackingSection<E> section = cache.getTrackingSection(ChunkSectionPos.asLong(x, y, z));
                    EntityTrackerSection sectionAccess = (EntityTrackerSection) section;
                    if (register) {
                        sectionAccess.addListener(this);
                        sortedSections.add(section);
                        sectionVisible.add(section.getStatus().shouldTrack());
                    } else {
                        sectionAccess.removeListener(cache, this);
                    }
                }
            }
        }
        this.setChanged();
        if (register) {
            this.sortedSections = sortedSections;
            this.sectionVisible = sectionVisible.toBooleanArray();
        } else {
            this.sortedSections = null;
            this.sectionVisible = null;
        }
    }

    public abstract List<S> getEntities();

    /**
     * Register an entity section to this listener, so this listener can look for changes in the section.
     */
    public void onSectionEnteredRange(Object section) {
        this.setChanged();
        if (this.sortedSections != null) {
            //noinspection SuspiciousMethodCalls
            this.sectionVisible[this.sortedSections.indexOf(section)] = true;
        }
    }

    public void onSectionLeftRange(Object section) {
        this.setChanged();
        //noinspection SuspiciousMethodCalls
        this.sectionVisible[this.sortedSections.indexOf(section)] = false;
    }

    private boolean checkForChanges() {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, registeredSectionsSize = this.sortedSections.size(); i < registeredSectionsSize; i++) {
            EntityTrackerSection trackedEntityList = (EntityTrackerSection) this.sortedSections.get(i);
            // >= instead of > is required here, as changes may occur in the same tick but after calling setUnchanged()
            if (trackedEntityList.getMovementTimestamp(this.trackedClass) >= this.lastCheckedTime) {
                this.setChanged();
                return true;
            }
        }
        return this.lastCheckedTime == Long.MIN_VALUE;
    }

    /**
     * Method that marks that new entities might have appeared or moved in the tracked chunk sections.
     */
    private void setChanged() {
        this.lastCheckedTime = Long.MIN_VALUE;
    }

    public void setUnchanged(long timestamp) {
        this.lastCheckedTime = timestamp;
    }
}
