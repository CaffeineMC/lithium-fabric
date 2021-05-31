package me.jellysquid.mods.lithium.common.entity.tracker.nearby;

import me.jellysquid.mods.lithium.common.util.collections.BucketedList;
import me.jellysquid.mods.lithium.mixin.block.hopper.EntityTrackingSectionAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.util.collection.TypeFilterableList;
import net.minecraft.util.math.Box;

import java.util.List;

public class NearbyItemEntityMovementTracker<S extends Entity> extends NearbyEntityMovementTracker<Entity, S> {
    private final Box[] interactionAreaBoxes;

    public NearbyItemEntityMovementTracker(Box encompassingBox, Box[] interactionArea, Class<S> clazz) {
        super(encompassingBox, clazz);
        this.interactionAreaBoxes = interactionArea;
    }


    @Override
    public List<S> getEntities() {
        BucketedList<S> entities = new BucketedList<>(this.interactionAreaBoxes.length);
        for (int sectionIndex = 0; sectionIndex < this.sortedSections.size(); sectionIndex++) {
            if (this.sectionVisible[sectionIndex]) {
                //noinspection unchecked
                TypeFilterableList<S> collection = ((EntityTrackingSectionAccessor<S>) this.sortedSections.get(sectionIndex)).getCollection();

                for (S entity : collection.getAllOfType(this.clazz)) {
                    if (entity.isAlive()) {
                        Box entityBoundingBox = entity.getBoundingBox();
                        if (entityBoundingBox.intersects(this.enclosingBox)) {
                            for (int j = 0; j < this.interactionAreaBoxes.length; j++) {
                                if (entityBoundingBox.intersects(this.interactionAreaBoxes[j])) {
                                    entities.addToBucket(j, entity);
                                }
                            }
                        }
                    }
                }
            }
        }
        return entities;
    }
}
