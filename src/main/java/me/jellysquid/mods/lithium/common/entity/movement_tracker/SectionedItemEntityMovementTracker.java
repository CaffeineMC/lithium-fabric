package me.jellysquid.mods.lithium.common.entity.movement_tracker;

import me.jellysquid.mods.lithium.common.util.tuples.WorldSectionBox;
import me.jellysquid.mods.lithium.common.world.LithiumData;
import me.jellysquid.mods.lithium.mixin.block.hopper.EntityTrackingSectionAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import java.util.ArrayList;
import java.util.List;

public class SectionedItemEntityMovementTracker<S extends Entity> extends SectionedEntityMovementTracker<Entity, S> {

    public SectionedItemEntityMovementTracker(WorldSectionBox worldSectionBox, Class<S> clazz) {
        super(worldSectionBox, clazz);
    }

    public static <S extends Entity> SectionedItemEntityMovementTracker<S> registerAt(ServerLevel world, AABB interactionArea, Class<S> clazz) {
        WorldSectionBox worldSectionBox = WorldSectionBox.entityAccessBox(world, interactionArea);
        SectionedItemEntityMovementTracker<S> tracker = new SectionedItemEntityMovementTracker<>(worldSectionBox, clazz);
        tracker = ((LithiumData) world).lithium$getData().entityMovementTrackers().getCanonical(tracker);

        tracker.register(world);
        return tracker;
    }

    public List<S> getEntities(AABB interactionArea) {
        ArrayList<S> entities = new ArrayList<>();
        for (int sectionIndex = 0; sectionIndex < this.sortedSections.size(); sectionIndex++) {
            if (this.sectionVisible[sectionIndex]) {
                //noinspection unchecked
                ClassInstanceMultiMap<S> collection = ((EntityTrackingSectionAccessor<S>) this.sortedSections.get(sectionIndex)).getCollection();

                for (S entity : collection.find(this.clazz)) {
                    if (entity.isAlive()) {
                        AABB entityBoundingBox = entity.getBoundingBox();
                        if (entityBoundingBox.intersects(interactionArea)) {
                            entities.add(entity);
                        }
                    }
                }
            }
        }
        return entities;
    }
}
