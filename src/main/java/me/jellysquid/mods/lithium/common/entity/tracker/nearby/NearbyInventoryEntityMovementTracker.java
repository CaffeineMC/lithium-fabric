package me.jellysquid.mods.lithium.common.entity.tracker.nearby;

import me.jellysquid.mods.lithium.mixin.block.hopper.EntityTrackingSectionAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.util.collection.TypeFilterableList;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

public class NearbyInventoryEntityMovementTracker<S> extends NearbyEntityMovementTracker<Entity, S> {

    public NearbyInventoryEntityMovementTracker(Box interactionArea, Class<S> clazz) {
        super(interactionArea, clazz);
    }

    @Override
    public List<S> getEntities() {
        ArrayList<S> entities = new ArrayList<>();
        for (int i = 0; i < this.sortedSections.size(); i++) {
            if (this.sectionVisible[i]) {
                //noinspection unchecked
                TypeFilterableList<S> collection = ((EntityTrackingSectionAccessor<S>) this.sortedSections.get(i)).getCollection();

                for (S entity : collection.getAllOfType(this.clazz)) {
                    Entity inventoryEntity = (Entity) entity;
                    if (inventoryEntity.isAlive() && inventoryEntity.getBoundingBox().intersects(this.enclosingBox)) {
                        entities.add(entity);
                    }
                }
            }
        }
        return entities;
    }
}
