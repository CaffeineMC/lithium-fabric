package me.jellysquid.mods.lithium.common.entity.tracker.nearby;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import me.jellysquid.mods.lithium.common.entity.tracker.EntityTrackerEngine;
import me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking.ServerEntityManagerAccessor;
import me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking.ServerWorldAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityTrackingSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class NearbyEntityMovementTracker<T extends EntityLike, S extends T> {
    private final Box box;
    private EntityTrackingSection<T>[] sortedSections;
    private boolean[] sectionVisible;
    private final int trackedClass;
    private final TypeFilter<T, S> typeFilter;
    private final Predicate<T> entityPredicate;

    private long lastCheckedTime = 0;

    public NearbyEntityMovementTracker(Box interactionArea, Class<S> clazz, Predicate<T> boxAndEntityPredicate) {
        this.box = interactionArea;
        this.trackedClass = EntityTrackerEngine.MOVEMENT_NOTIFYING_ENTITY_CLASSES.indexOf(clazz);
        assert this.trackedClass != -1;
        this.typeFilter = TypeFilter.instanceOf(clazz);
        this.entityPredicate = boxAndEntityPredicate; //suggested value: (entity) -> entity.getBoundingBox().intersects(this.box) && entity.isAlive();
    }

    public void register(ServerWorld world) {
        this.updateRegistration(world, true);
    }

    public void unRegister(ServerWorld world) {
        this.updateRegistration(world, false);
    }

    private void updateRegistration(ServerWorld world, boolean register) {
        ArrayList<EntityTrackingSection<? extends EntityLike>> sortedSections = register ? new ArrayList<>() : null;
        BooleanArrayList sectionVisible = register ? new BooleanArrayList() : null;
        int minX = ChunkSectionPos.getSectionCoord(this.box.minX - 2.0D);
        int minY = ChunkSectionPos.getSectionCoord(this.box.minY - 2.0D);
        int minZ = ChunkSectionPos.getSectionCoord(this.box.minZ - 2.0D);
        int maxX = ChunkSectionPos.getSectionCoord(this.box.maxX + 2.0D);
        int maxY = ChunkSectionPos.getSectionCoord(this.box.maxY + 2.0D);
        int maxZ = ChunkSectionPos.getSectionCoord(this.box.maxZ + 2.0D);
        //vanilla iteration order in SectionedEntityCache is xzy
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    EntityTrackingSection<? extends EntityLike> section = ((ServerEntityManagerAccessor) ((ServerWorldAccessor) world).getEntityManager()).getCache().getTrackingSection(ChunkSectionPos.asLong(x, y, z));
                    EntityTrackerEngine.EntityTrackingSectionAccessor sectionAccess = (EntityTrackerEngine.EntityTrackingSectionAccessor) section;
                    if (register) {
                        sectionAccess.addListener(this);
                        sortedSections.add(section);
                        sectionVisible.add(section.getStatus().shouldTrack());
                    } else {
                        sectionAccess.removeListener(this);
                    }
                }
            }
        }
        this.setChanged();
        if (register) {
            //noinspection unchecked
            this.sortedSections = (EntityTrackingSection<T>[]) sortedSections.toArray();
            this.sectionVisible = sectionVisible.toBooleanArray();
        }
    }

    public List<S> getEntitiesInBox() {
        ArrayList<S> entities = new ArrayList<>();
        for (int i = 0; i < this.sortedSections.length; i++) {
            if (this.sectionVisible[i]) {
                this.sortedSections[i].forEach(this.typeFilter, this.entityPredicate, entities::add);
            }
        }
        return entities;
    }

    /**
     * Register an entity section to this listener, so this listener can look for changes in the section.
     */
    public void onSectionEnteredRange(Object section) {
        if (section instanceof EntityTrackerEngine.EntityTrackingSectionAccessor newSection) {
            this.setChanged();
            this.sectionVisible[Arrays.binarySearch(this.sortedSections, section)] = true;
        }
    }

    public void onSectionLeftRange(Object entityTrackingSection) {
        if (entityTrackingSection instanceof EntityTrackerEngine.EntityTrackingSectionAccessor section) {
            this.setChanged();
            this.sectionVisible[Arrays.binarySearch(this.sortedSections, section)] = false;
        }
    }


    public boolean hasChanged() {
        return this.lastCheckedTime == 0 || checkForChanges();
    }

    private boolean checkForChanges() {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, registeredSectionsSize = this.sortedSections.length; i < registeredSectionsSize; i++) {
            EntityTrackerEngine.EntityTrackingSectionAccessor trackedEntityList = (EntityTrackerEngine.EntityTrackingSectionAccessor) this.sortedSections[i];
            if (trackedEntityList.getMovementTimestamp(this.trackedClass) >= this.lastCheckedTime) {
                this.setChanged();
                return true;
            }
        }
        return this.lastCheckedTime == 0;
    }

    /**
     * Method that marks that new entities have appeared or moved in the nearby chunk sections.
     */
    private void setChanged() {
        this.lastCheckedTime = 0;
    }

    public void setUnchanged(long timestamp) {
        this.lastCheckedTime = timestamp;
    }
}
