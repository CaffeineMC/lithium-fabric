package me.jellysquid.mods.lithium.common.entity.movement_tracker;

import me.jellysquid.mods.lithium.common.util.tuples.WorldSectionBox;
import me.jellysquid.mods.lithium.common.world.LithiumData;
import me.jellysquid.mods.lithium.mixin.block.hopper.EntityTrackingSectionAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.TypeFilterableList;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

public class SectionedItemEntityMovementTracker<S extends Entity> extends SectionedEntityMovementTracker<Entity, S> {

    public SectionedItemEntityMovementTracker(WorldSectionBox worldSectionBox, Class<S> clazz) {
        super(worldSectionBox, clazz);
    }

    public static <S extends Entity> SectionedItemEntityMovementTracker<S> registerAt(ServerWorld world, Box interactionArea, Class<S> clazz) {
        WorldSectionBox worldSectionBox = WorldSectionBox.entityAccessBox(world, interactionArea);
        SectionedItemEntityMovementTracker<S> tracker = new SectionedItemEntityMovementTracker<>(worldSectionBox, clazz);
        tracker = ((LithiumData) world).lithium$getData().entityMovementTrackers().getCanonical(tracker);

        tracker.register(world);
        return tracker;
    }

    public List<S> getEntities(Box interactionArea) {
        ArrayList<S> entities = new ArrayList<>();
        for (int sectionIndex = 0; sectionIndex < this.sortedSections.size(); sectionIndex++) {
            if (this.sectionVisible[sectionIndex]) {
                //noinspection unchecked
                TypeFilterableList<S> collection = ((EntityTrackingSectionAccessor<S>) this.sortedSections.get(sectionIndex)).getCollection();

                for (S entity : collection.getAllOfType(this.clazz)) {
                    if (entity.isAlive()) {
                        Box entityBoundingBox = entity.getBoundingBox();
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
